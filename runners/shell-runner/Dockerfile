FROM mcr.microsoft.com/powershell:lts-debian-12

RUN apt-get update \
    && apt-get install --no-install-recommends --yes bash coreutils findutils grep sed gawk diffutils git jq make tree less \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 runner \
    && useradd --system --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin runner

# L'image PowerShell impose LC_ALL=en_US.UTF-8, locale absente ici : sans ce remplacement, chaque commande
# afficherait un avertissement setlocale sur stderr.
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    POWERSHELL_TELEMETRY_OPTOUT=1 \
    DOTNET_CLI_TELEMETRY_OPTOUT=1 \
    DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=1

WORKDIR /workspace
USER 10001:10001
