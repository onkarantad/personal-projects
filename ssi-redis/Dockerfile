FROM openjdk:8-jdk-alpine
WORKDIR /opt/cache
COPY cache.sh .
COPY target .
RUN  ls -R /opt/cache
RUN chmod +x /opt/cache/cache.sh
EXPOSE 3275/tcp
CMD ["sh", "cache.sh"]