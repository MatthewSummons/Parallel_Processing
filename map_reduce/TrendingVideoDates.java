import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class TrendingVideoDates {

    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("TrendingVideoDates")
                .master("local[*]") // Use local master for testing. Change for your cluster.
                .getOrCreate();

        // Define the schema for the preprocessed CSV.
        // The 'publish_time' field is now just a date string (e.g., "YYYY-MM-DD").
        // Other fields are assumed to be the same as the original CSV.
        StructType schema = new StructType()
                .add("video_id", DataTypes.StringType, true)
                .add("trending_date", DataTypes.StringType, true)
                .add("title", DataTypes.StringType, true)
                .add("channel_title", DataTypes.StringType, true)
                .add("category_id", DataTypes.StringType, true) // Kept as StringType, adjust if known to be Integer
                .add("publish_time", DataTypes.StringType, true) // This is our key field for aggregation
                .add("tags", DataTypes.StringType, true)
                .add("views", DataTypes.LongType, true)
                .add("likes", DataTypes.LongType, true)
                .add("dislikes", DataTypes.LongType, true)
                .add("comment_count", DataTypes.LongType, true)
                .add("thumbnail_link", DataTypes.StringType, true)
                .add("comments_disabled", DataTypes.BooleanType, true)
                .add("ratings_disabled", DataTypes.BooleanType, true)
                .add("video_error_or_removed", DataTypes.BooleanType, true)
                .add("description", DataTypes.StringType, true);

        // Path to your preprocessed CSV file
        String csvFilePath = "./output/Ziya_USVideos/proc.csv";

        // Read the preprocessed CSV file
        Dataset<Row> videosDf = spark.read()
                .option("header", "true") // Assuming the preprocessed file still has a header
                .option("escape", "\"")
                .schema(schema)
                .csv(csvFilePath);

        // Aggregate the number of videos by their publish_time (which is now just the date)
        // then rank them accordingly.
        Dataset<Row> videosByDateDf = videosDf
                .groupBy("publish_time")
                .count() // This will create a column named "count" with the number of videos per date
                .orderBy(functions.col("count").desc()) // Order by the count in descending order
                .limit(10); // Get the top ten dates

        // Show the results in a tabular format (optional, but good for quick view)
        System.out.println("Top 10 dates with the highest number of videos published (table format):");
        videosByDateDf.show(false);

        // Print the results in the specified format: (YYYY-MM-DD, Count)
        System.out.println("\nTop 10 dates with the highest number of videos published (custom format):");
        videosByDateDf.foreach(row -> {
            String publishDate = row.getString(0); // First column is 'publish_time'
            long videoCount = row.getLong(1);      // Second column is 'count'
            System.out.println("(\"" + publishDate + "\"," + videoCount + ")");
        });

        // Stop the SparkSession to release resources
        spark.stop();
    }
}