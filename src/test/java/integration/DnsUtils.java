package integration;

import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DnsUtils {

    public static Response forNameType(Resolver resolver, String nameStr, int type) {
        Name name;

        try {
            name = Name.fromString(nameStr + ".");
        } catch (TextParseException e) {
            throw new IllegalArgumentException();
        }
        Lookup lookup = new Lookup(name, type);
        lookup.setResolver(resolver);
        lookup.setCache(null);
        lookup.setCredibility(Credibility.ZONE);
        lookup.run();

        int result = lookup.getResult();
        if (result != Lookup.SUCCESSFUL) {
            return new Response(result, List.of());
        }
        return new Response(result, Arrays.stream(lookup.getAnswers())
                .map(Record::rdataToString)
                .collect(Collectors.toList()));
    }
}
