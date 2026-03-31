# Usamos la imagen oficial
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# PARCHE: Borramos el repositorio roto de Yarn ANTES de que VS Code instale nada
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && rm -f /etc/apt/sources.list.d/yarnpkg.list \
    && apt-get update