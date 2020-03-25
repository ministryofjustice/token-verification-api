{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "logstash"

  - name: APPLICATION_INSIGHTS_IKEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: SPRING_REDIS_HOST
    valueFrom:
      secretKeyRef:
        name: tva-elasticache-redis
        key: primary_endpoint_address

  - name: SPRING_REDIS_PASSWORD
    valueFrom:
      secretKeyRef:
        name: tva-elasticache-redis
        key: auth_token
{{- end -}}
