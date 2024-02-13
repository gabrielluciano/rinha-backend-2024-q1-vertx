# Rinha Backend 2024 Q1 Java + Vert.x

Esta é minha submissão para a [Rinha de Backend - 2024/Q1](https://github.com/zanfranceschi/rinha-de-backend-2024-q1) utilizando Java com a lib Vert.x.

A imagem do docker atual utiliza um executável nativo, compilado utilizando GraalVM.

## Tecnologias utilizadas

- Java com a lib [Eclipse Vert.x](https://vertx.io/) 
  - Vert.x Web 
  - Vert.x Reactive PostgreSQL Client
- PostgreSQL
- Nginx
- Compilação para executável nativo utilizando GraalVM

## Minhas redes sociais

- [GitHub](https://github.com/gabrielluciano)
- [Linkedin](https://www.linkedin.com/in/gabriel-lucianosouza/)
- [Twitter](https://twitter.com/biel_luciano)

## Getting Started

```shell
# Clonando o repo
git clone https://github.com/gabrielluciano/rinha-backend-2024-q1-vertx
cd rinha-backend-2024-q1-vertx

# Iniciando o projeto com o Docker Compose
docker compose up -d
```

## Compilação para executável nativo e build da docker image

Para isso é necessário ter o GraalVM instalado na versão 21

```shell
cd rinha-backend-2024-q1-vertx

# Gerar o JAR
mvn clean package -DskipTests

# Compilar para executável nativo
native-image -jar target/rinha-1.0.0.jar --no-fallback --gc=G1 -march=compatibility

# Build da imagem
docker build -t <nome-da-imagem>:<tag> .
```

## Erros conhecidos

- Ao executar o stress test da rinha em uma máquina diferente da de onde o binário nativo foi gerado, as primeiras requisições podem causar erro de timeout. Ainda estou investigando as causas.
