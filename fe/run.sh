#!/bin/bash

clear

PORT=8081
TAG=dev

set -x

mvn clean package && \
docker build -t acessoatodos/fe:${TAG} . && \
docker run -d --name acessoatodos-fe -p $PORT:$PORT acessoatodos-fe:${TAG}