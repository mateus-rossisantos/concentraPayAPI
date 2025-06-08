# Imagem base com Java JDK 17 (você pode trocar para 21, se quiser)
FROM eclipse-temurin:17-jdk-alpine

# Diretório de trabalho dentro do container
WORKDIR /app

# Copia o JAR gerado para o container (substitua pelo nome real do seu JAR)
COPY target/minha-aplicacao.jar app.jar

# Expõe a porta que a aplicação Spring Boot usa (geralmente 8080)
EXPOSE 8080

# Comando para rodar o JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
