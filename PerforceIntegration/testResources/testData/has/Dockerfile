# --------------------------------------------------------------------------------
# Docker configuration for P4D
# --------------------------------------------------------------------------------
FROM ubuntu:xenial

LABEL vendor="Perforce Software"
LABEL maintainer="Paul Allen (pallen@perforce.com)"

# Update Ubuntu and add Perforce Package Source
RUN apt-get update
RUN apt-get install -y wget unzip vim
RUN wget -qO - https://package.perforce.com/perforce.pubkey | apt-key add - 
RUN echo "deb http://package.perforce.com/apt/ubuntu xenial release" > /etc/apt/sources.list.d/perforce.list 
RUN apt-get update

# Create perforce user and install Perforce Server
RUN apt-get install -y helix-p4d helix-cli
RUN /opt/perforce/sbin/configure-helix-p4d.sh -n -p 1666 -u super -P Password main

WORKDIR /perforce

# Add external files
COPY files/build.sh .
COPY files/run.sh .
COPY files/main.lua loginhook/
COPY files/manifest.json loginhook/
COPY files/ext_config.awk .
COPY files/user_swarm.txt .
COPY files/user_jack.txt .
COPY files/user_john.txt .
COPY files/group_unlimited.txt .


RUN ./build.sh

EXPOSE 1666

ENTRYPOINT [ "./run.sh" ]
