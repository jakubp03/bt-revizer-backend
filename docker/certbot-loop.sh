#!/bin/sh
set -eu

staging_flag=""
if [ "${CERTBOT_STAGING:-0}" = "1" ]; then
  staging_flag="--staging"
fi

issue_cert() {
  certbot certonly --webroot -w /var/www/certbot \
    --email "${CERTBOT_EMAIL}" \
    --agree-tos \
    --no-eff-email \
    --non-interactive \
    ${staging_flag} \
    -d "${DOMAIN_NAME}"
}

copy_cert() {
  cp "/etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem" /shared-certs/fullchain.pem
  cp "/etc/letsencrypt/live/${DOMAIN_NAME}/privkey.pem" /shared-certs/privkey.pem
}

mkdir -p /shared-certs

if [ ! -f "/etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem" ]; then
  issue_cert || true
fi

if [ -f "/etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem" ]; then
  copy_cert
fi

while true; do
  certbot renew --webroot -w /var/www/certbot ${staging_flag} --quiet || true

  if [ -f "/etc/letsencrypt/live/${DOMAIN_NAME}/fullchain.pem" ]; then
    copy_cert
  fi

  sleep 12h
done