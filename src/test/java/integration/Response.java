package integration;

import java.util.List;
import java.util.Objects;

public record Response(int statut, List<String> results) {

    public Response {
        Objects.requireNonNull(results);
    }
}
