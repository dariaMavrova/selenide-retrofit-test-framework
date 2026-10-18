# Jenkins controller with the plugins and configuration baked in: no setup wizard, no clicking.
FROM jenkins/jenkins:2.580.1-lts-jdk21

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false" \
    CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml

COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY --chown=jenkins:jenkins casc.yaml /var/jenkins_home/casc.yaml
