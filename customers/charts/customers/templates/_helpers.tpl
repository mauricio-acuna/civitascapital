{{/*
Expand the name of the chart.
*/}}
{{- define "magenta-customers.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Full name: release-chart, max 63 chars.
*/}}
{{- define "magenta-customers.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels.
*/}}
{{- define "magenta-customers.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
app.kubernetes.io/name: {{ include "magenta-customers.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels.
*/}}
{{- define "magenta-customers.selectorLabels" -}}
app.kubernetes.io/name: {{ include "magenta-customers.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
