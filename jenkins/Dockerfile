# Jenkins controller with the plugins and configuration baked in: no setup wizard, no clicking.
FROM jenkins/jenkins:2.580.1-lts-jdk21

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false" \
    # Outside JENKINS_HOME (a volume), so a rebuilt image always applies the current config.
    CASC_JENKINS_CONFIG=/usr/share/jenkins/casc/casc.yaml

# Allure CLI baked in (pinned + checksum) instead of the plugin's runtime download, which is fragile.
ARG ALLURE_VERSION=2.46.1
ARG ALLURE_SHA256=d25c519bbde940dc953cff8901130ce18b73013dc19a8f8b90b8b4b1c5eb252f
USER root
RUN curl -fsSL -o /tmp/allure.zip \
        "https://repo1.maven.org/maven2/io/qameta/allure/allure-commandline/${ALLURE_VERSION}/allure-commandline-${ALLURE_VERSION}.zip" \
    && echo "${ALLURE_SHA256}  /tmp/allure.zip" | sha256sum -c - \
    && unzip -q /tmp/allure.zip -d /opt \
    && ln -s "/opt/allure-${ALLURE_VERSION}" /opt/allure \
    && rm /tmp/allure.zip
USER jenkins

COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY --chown=jenkins:jenkins casc.yaml /usr/share/jenkins/casc/casc.yaml
