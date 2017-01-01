FROM java:8
RUN apt-get update && apt-get install -y dnsutils
ADD master-rx.jar /
RUN touch /master-rx.jar
EXPOSE 8080
CMD ["java", "-jar", "master-rx.jar"]