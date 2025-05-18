package com.ev.apiservice.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.locationtech.jts.geom.Point; // Import for Point type check
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    // Fields that contain potentially sensitive information from Entities and DTOs
    private static final String[] SENSITIVE_FIELDS = {
            "vin", "dolVehicleId", "county", "city", "state", "postalCode",
            "vehicleLocation", // From DTO
            "vehicleLocationPoint", // From Entity
            "latitude", // Likely from PointDTO
            "longitude" // Likely from PointDTO
    };

    // Pattern to mask VINs that are exactly 10 characters long: reveal first 3 and last 2, mask the middle 5
    private static final Pattern VIN_PATTERN = Pattern.compile("([A-Z0-9]{3})[A-Z0-9]{5}([A-Z0-9]{2})");

    // Pattern to mask sensitive IDs (e.g., dolVehicleId)
    private static final Pattern ID_PATTERN = Pattern.compile("(\\d{3})\\d+");

    public LoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* com.ev.apiservice.controller.*.*(..))")
    private void controllerMethods() {}

    @Pointcut("execution(* com.ev.apiservice.service.*.*(..))")
    private void serviceMethods() {}

    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Map<String, Object> logData = new HashMap<>();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        logData.put("class", className);
        logData.put("method", methodName);

        // Log sanitized method arguments
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            logData.put("args", sanitizeArgs(args));
        }

        log.info("Starting execution: {}", toJson(logData));

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        logData.put("duration_ms", duration);

        // Log sanitized result
        if (result != null) {
            logData.put("result", sanitizeObject(result));
        }

        log.info("Completed execution: {}", toJson(logData));
        return result;
    }

    private Object sanitizeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::sanitizeObject)
                .toArray();
    }

    private Object sanitizeObject(Object obj) {
        if (obj == null) return null;

        // Handle specific sensitive types directly if possible
        if (obj instanceof Point) {
            return "[LOCATION_POINT_REDACTED]"; // Mask Point object directly (from Entity)
        }
        // Note: PointDTO will be handled when converting the parent object to a Map

        // Handle primitive wrappers, Numbers, Booleans, Strings directly
        if (obj.getClass().isPrimitive() || obj instanceof Number || obj instanceof Boolean || obj instanceof String) {
            // Strings will be checked against SENSITIVE_FIELDS when iterating through a Map
            return obj;
        }


        try {
            // Convert to map for complex objects (entities, DTOs, nested DTOs like PointDTO)
            Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
            Map<String, Object> sanitized = new HashMap<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Check if the field name itself is sensitive, or if its value is a specific sensitive type
                if (isSensitiveField(key)) {
                    sanitized.put(key, maskSensitiveData(key, value));
                } else {
                    // Recursively sanitize nested objects/collections
                    sanitized.put(key, sanitizeObject(value));
                }
            }
            return sanitized;
        } catch (IllegalArgumentException e) {
            // Handle cases where convertValue fails (e.g., object is not a simple POJO that ObjectMapper can handle)
            log.warn("Could not convert object for sanitization: {}", obj.getClass().getName());
            return obj.getClass().getSimpleName() + "(non-POJO logged object)";
        } catch (Exception e) {
            // Catch any other exceptions during processing
            log.error("Error during object sanitization: {}", obj.getClass().getName(), e);
            // If we can't process it, return a placeholder
            return obj.getClass().getSimpleName() + "(logging error)";
        }
    }

    // Check if the field name is in our list of sensitive fields (case-insensitive)
    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) return false;
        for (String sensitiveField : SENSITIVE_FIELDS) {
            if (fieldName.equalsIgnoreCase(sensitiveField)) {
                return true;
            }
        }
        return false;
    }

    private Object maskSensitiveData(String fieldName, Object value) {
        if (value == null) return null;

        // Specific masking logic based on field name
        if (fieldName.equalsIgnoreCase("vin")) {
            // Ensure value is treated as a String for VIN masking
            if (value instanceof String) {
                return maskVin((String) value);
            }
            return "[VIN_MASKING_ERROR]"; // Indicate if VIN field value is not a String
        } else if (fieldName.equalsIgnoreCase("dolVehicleId")) {
            // Ensure value is treated as a String for ID masking
            return maskId(value.toString()); // toString handles Long to String conversion
        } else if (fieldName.equalsIgnoreCase("county") ||
                fieldName.equalsIgnoreCase("city") ||
                fieldName.equalsIgnoreCase("state")) {
            return "[LOCATION_REDACTED]";
        } else if (fieldName.equalsIgnoreCase("postalCode")) {
            // Assuming postalCode is a string, mask first few chars
            if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue.length() > 3) {
                    return stringValue.substring(0, 3) + "***";
                }
                return "***"; // Mask if 1-3 chars
            }
            return "[POSTAL_CODE_MASKING_ERROR]"; // Indicate if postalCode field value is not a String
        } else if (fieldName.equalsIgnoreCase("vehicleLocation") ||
                fieldName.equalsIgnoreCase("vehicleLocationPoint")) {
            // For the field name itself, just indicate it's location data that will be masked internally
            // The recursive call in sanitizeObject will handle the actual Point/PointDTO value masking
            return "[LOCATION_DATA]";
        } else if (fieldName.equalsIgnoreCase("latitude") ||
                fieldName.equalsIgnoreCase("longitude")) {
            return "[COORD_REDACTED]"; // Mask latitude/longitude coordinates
        }


        // Default masking for fields in SENSITIVE_FIELDS that don't have more specific logic above
        return "[REDACTED]";
    }

    private String maskVin(String vin) {
        // Only mask if the VIN is exactly 10 characters long
        if (vin == null || vin.length() != 10) {
            return vin; // Return original if not exactly 10 characters or null
        }
        // Mask the middle 5 characters, revealing the first 3 and last 2
        return VIN_PATTERN.matcher(vin).replaceAll("$1*****$2");
    }

    private String maskId(String id) {
        // Mask IDs like dolVehicleId
        if (id == null || id.length() < 4) return id;
        // Mask all but the first 3 digits
        return ID_PATTERN.matcher(id).replaceAll("$1***");
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON for logging", e);
            return "Error serializing object: " + obj.getClass().getName();
        }
    }
}