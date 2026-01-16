package com.ridei.identity.infrastructure.adapter.in.web.dto;

// Usamos un Generic <T> por si en el futuro queremos devolver datos (ej: el ID del usuario creado)
// Por ahora, T puede ser Void si no devolvemos datos.
public record ApiResponse<T>(
    int code,
    String Message,
    T data
) {  
    // Constructor estático de conveniencia para respuestas sin datos (solo mensaje)
    public static ApiResponse<Void> success(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // Constructor para respuestas con datos
    public static <T> ApiResponse<T> success(int code, String meString, T data) {
        return new ApiResponse<T>(code, meString, data);
    }
}
