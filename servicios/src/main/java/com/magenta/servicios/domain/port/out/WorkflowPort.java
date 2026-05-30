package com.magenta.servicios.domain.port.out;

import java.util.Map;
import java.util.UUID;

public interface WorkflowPort {
    String startProcess(String processKey, UUID orderId, Map<String, Object> variables);
    void publishMessage(String messageName, String correlationKey, Map<String, Object> variables);
    void cancelProcess(String workflowInstanceId, String reason);
}
