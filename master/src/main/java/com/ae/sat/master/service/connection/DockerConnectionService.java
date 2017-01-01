package com.ae.sat.master.service.connection;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by ae on 17-12-16.
 */
@Component
public class DockerConnectionService  {

    public int debugPort = 5005;

    @Value("${dockerUsername}")
    private String dockerUsername;

    @Value("${dockerPassword}")
    private String dockerPassword;

    @Value("${dockerEmail}")
    private String dockerEmail;

    @Value("${dockerApiVersion}")
    private String dockerApiVersion;

    @Value("${localDockerIP}")
    private String localDockerIP;

    @Value("${localDockerPort}")
    private int localDockerPort;

    @Value("${workerImageName}")
    private String workerImageName;

    @Value("${workerPort}")
    private int workerPort;

    @Value("${workerWsPort}")
    private int workerWsPort;

    private boolean doDebug = false;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DockerClient getClient() {
        String dockerUrl = String.format("tcp://%s:%s", localDockerIP, localDockerPort);

        DefaultDockerClientConfig.Builder builder;
        builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                                                .withDockerTlsVerify(false)
                                                .withDockerHost(dockerUrl);

        if (!StringUtils.isEmpty(dockerApiVersion)) {
            builder.withApiVersion(dockerApiVersion); // Terrible hack!
        }
        final boolean hasAuth = !StringUtils.isEmpty(dockerUsername)
                && !("\"\"".equals(dockerUsername));
        if (hasAuth) {
            log.debug("Logging into docker with {} provided ..", dockerUsername);
            builder = builder.withRegistryEmail(dockerEmail)
                             .withRegistryUsername(dockerUsername)
                             .withRegistryPassword(dockerPassword);
        } else {
            log.debug("No docker username provided ..");
        }

        final DockerClientBuilder dockerClientBuilder;
        dockerClientBuilder = DockerClientBuilder.getInstance(builder.build());

        DockerClient dockerClient = dockerClientBuilder.build();
        if (hasAuth) {
            dockerClient.authCmd().exec();
        }
        return dockerClient;
    }

    private class DockerConnection implements Connection {

        private int actualHttpPort = -1;

        private int actualWsPort = -1;

        private DockerClient dockerClient;

        private CreateContainerResponse container;

        private String name;

        public DockerConnection(String name) {
            this.name = name;
        }

        public boolean initialized() {
            return dockerClient != null;
        }

        private void initialize() {
            dockerClient = getClient();

            String tagName = String.format("%s:latest", workerImageName);
            boolean needsImage = dockerClient.listImagesCmd()
                    .exec()
                    .stream()
                    .map(f -> Arrays.asList(f.getRepoTags()))
                    .noneMatch(f -> f.contains(tagName));

            if (needsImage) {
                log.info("{} Pulling SAT worker {}, might take a while ..",
                        name,
                        workerImageName);
                dockerClient.pullImageCmd(workerImageName)
                        .exec(new PullImageResultCallback())
                        .awaitSuccess();
                log.info("{} Done with pulling creating the container now ..", name);
            }

            ExposedPort exposedWorkerPort = ExposedPort.tcp(workerPort);
            ExposedPort exposedWSWorkerPort = ExposedPort.tcp(workerWsPort);
            ExposedPort exposedDebugPort = ExposedPort.tcp(debugPort);

            Ports portBindings = new Ports();
            portBindings.bind(exposedWorkerPort, Ports.Binding.empty());
            portBindings.bind(exposedWSWorkerPort, Ports.Binding.empty());
            portBindings.bind(exposedDebugPort, Ports.Binding.empty());

            //
            // Create & start container
            //
            String[] command = new String[] {
                    "sh",
                    "-c",
                    "java " +
                            (doDebug ? "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 " : "") +
                            "-Dlogging.level.com.ae.sat=DEBUG " +
                            "-jar worker-rx.jar "
            };

            container = dockerClient.createContainerCmd(workerImageName)
                    .withName(name)
                    .withExposedPorts(exposedWorkerPort,
                            exposedWSWorkerPort,
                            exposedDebugPort)
                    .withPortBindings(portBindings)
                    .withCmd(command)
                    .exec();
            dockerClient.startContainerCmd(container.getId()).exec();

            //
            // Check on which port it is running.
            //
            InspectContainerResponse inspectResponse;
            inspectResponse = dockerClient.inspectContainerCmd(container.getId()).exec();

            Map<ExposedPort, Ports.Binding[]> ports = inspectResponse.getNetworkSettings()
                    .getPorts()
                    .getBindings();
            Ports.Binding[] bindings;
            bindings = ports.get(exposedWorkerPort);
            actualHttpPort = Integer.parseInt(bindings[0].getHostPortSpec()); // Integer.parseInt(exposedBinding.getHostPortSpec());
            bindings = ports.get(exposedWSWorkerPort);
            actualWsPort = Integer.parseInt(bindings[0].getHostPortSpec()); // Integer.parseInt(exposedBinding.getHostPortSpec());
        }

        @Override
        public int httpPort() {
            if (!initialized()) {
                initialize();
            }
            return actualHttpPort;
        }

        @Override
        public int wsPort() {
            if (!initialized()) {
                initialize();
            }
            return actualWsPort;
        }

        @Override
        public String host() {
            return localDockerIP;
        }

        @Override
        public void close() throws IOException {
            log.info("Killing {} ..", name);
            dockerClient.close();
        }
    }

    public Connection getNewConnection(String name) {
        return new DockerConnection(name);
    }
}
