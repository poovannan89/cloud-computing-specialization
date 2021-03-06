import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Double.valueOf;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;

/**
 * @author <a href="mailto:kgrodzicki@gmail.com">Krzysztof Grodzicki</a> 28/01/16.
 */
public class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

    public static final String SEPARATORS = "\n";

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        List<ImmutablePair<String, Integer>> pairs = parse(value.toString());
        for (ImmutablePair<String, Integer> pair : pairs) {
            context.write(new Text(pair.left), new IntWritable(pair.right));
        }
    }

    List<ImmutablePair<String, Integer>> parse(String value) {
        List<ImmutablePair<String, Integer>> result = newArrayList();
        StringTokenizer tokenizer = new StringTokenizer(value, SEPARATORS);
        while (tokenizer.hasMoreElements()) {
            String line = (String) tokenizer.nextElement();
            String[] parsed = line.split(",", -1);
            String origin = parsed[0];
            String delay = parsed[1];
            String dest = parsed[2];
            // skip rows which where cancelled
            result.add(of(merge(origin, dest), onTime(delay)));
        }
        return result;
    }

    private String merge(String origin, String dest) {
        return origin + "-" + dest;
    }

    private Integer onTime(String dapartureDelay) {
        if (isNotBlank(dapartureDelay) && valueOf(dapartureDelay) <= 0) {
            return 1;
        }
        return 0;
    }
}
