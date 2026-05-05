# Usamos Ubuntu antiguo compatible con 32 bits, forzando arquitectura Intel
FROM --platform=linux/amd64 ubuntu:18.04

# Instalamos GCC y librerías de 32 bits una sola vez
RUN apt-get update && \
    apt-get install -y gcc gcc-multilib libc6-dev && \
    rm -rf /var/lib/apt/lists/*

# Definimos la carpeta de trabajo
WORKDIR /app