# 基础镜像
FROM openjdk:8-jre-slim
# 作者
MAINTAINER jingtao8a
# 配置
ENV PARAMS=""
# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 添加应用  将文件、目录或远程URL复制到镜像中
ADD target/chatgpt-api.jar /chatgpt-api.jar
## 在镜像运行为容器后执行的命令
ENTRYPOINT ["sh", "-c", "java -jar $JAVA_OPS /chatgpt-api.jar $PARAMS"]
