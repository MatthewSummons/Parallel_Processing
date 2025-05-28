import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CSVPreprocessor {

    private static final int NUM_EXPECTED_FIELDS = 16;
    private static final int PUBLISH_TIME_INDEX = 5;
    private static final String HEADER_FIRST_COLUMN_NAME = "video_id";
    private static final String HEADER_PUBLISH_TIME_COLUMN_NAME = "publish_time";

    public static class CSVMapper extends Mapper<LongWritable, Text, NullWritable, Text> {

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = parseCsvLine(line);

            if (parts.length == NUM_EXPECTED_FIELDS) {
                if (isHeaderRow(parts)) {
                    context.write(NullWritable.get(), new Text(line));
                } else {
                    String modifiedPublishTime = formatPublishTime(parts[PUBLISH_TIME_INDEX]);
                    parts[PUBLISH_TIME_INDEX] = modifiedPublishTime;
                    context.write(NullWritable.get(), new Text(reconstructCsvLine(parts)));
                }
            } else {
                // Malformed line, skip it
                System.err.println("Skipping malformed line (expected " + NUM_EXPECTED_FIELDS + " fields, found " + parts.length + "): " + line);
            }
        }

        private String[] parseCsvLine(String line) {
            boolean inQuotes = false;
            StringBuilder currentField = new StringBuilder();
            String[] parts = new String[NUM_EXPECTED_FIELDS];
            int fieldIndex = 0;

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    parts[fieldIndex++] = currentField.toString().trim();
                    currentField.setLength(0);
                } else {
                    currentField.append(c);
                }
            }

            parts[fieldIndex] = currentField.toString().trim();
            return parts;
        }

        private boolean isHeaderRow(String[] parts) {
            return parts[0].trim().equals(HEADER_FIRST_COLUMN_NAME) &&
                   parts[PUBLISH_TIME_INDEX].trim().equals(HEADER_PUBLISH_TIME_COLUMN_NAME);
        }

        private String formatPublishTime(String originalPublishTime) {
            if (originalPublishTime == null || originalPublishTime.isEmpty()) {
                // Handle null or empty publish_time field
                return originalPublishTime;
            } else if (originalPublishTime.length() > 10 && originalPublishTime.charAt(10) == 'T' && originalPublishTime.endsWith("Z")) {
                return originalPublishTime.substring(0, 10);
            } else {
                // Keep the original value if it's not in the expected format
                return originalPublishTime;
            }
        }

        private String reconstructCsvLine(String[] parts) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(parts[i]);
                if (i < parts.length - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }

    public static class PassthroughReducer extends Reducer<NullWritable, Text, NullWritable, Text> {
        @Override
        public void reduce(NullWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            for (Text value : values) {
                context.write(NullWritable.get(), value);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: CSVPreprocessor <input_path> <output_path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "CSV Video Data Preprocessor");

        job.setJarByClass(CSVPreprocessor.class);

        job.setMapperClass(CSVMapper.class);
        job.setReducerClass(PassthroughReducer.class);

        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Optional: Set the number of reduce tasks to 0 for a map-only job
        // job.setNumReduceTasks(0);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}