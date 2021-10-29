package sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.ResponseConverterFunction;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JsonResponseConverter implements ResponseConverterFunction {
    private final ObjectMapper objectMapper;

    @Override
    public HttpResponse convertResponse(ServiceRequestContext ctx, ResponseHeaders headers,
                                        @Nullable Object result, HttpHeaders trailers) throws Exception {
        if (!ctx.config().route().produces().isEmpty()) {
            return ResponseConverterFunction.fallthrough();
        }

        final String json = objectMapper.writeValueAsString(result);
        return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, json);
    }
}
