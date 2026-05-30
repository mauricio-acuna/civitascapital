{{/*
Expand the name of the chart.
*/}}
{{- define "servicios.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "servicios.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "servicios.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "servicios.labels" -}}
helm.sh/chart: {{ include "servicios.name" . }}-{{ .Chart.Version }}
{{ include "servicios.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "servicios.selectorLabels" -}}
app.kubernetes.io/name: {{ include "servicios.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "servicios.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{ default (include "servicios.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
{{ default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}
