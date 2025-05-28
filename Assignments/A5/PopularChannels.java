import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class PopularChannels {

        public static void main(String[] args) {
                // Initialize SparkSession
                SparkSession spark = SparkSession.builder()
                                .appName("PopularChannels")
                                .master("local[*]") // Use local master for testing. Change for your cluster.
                                .getOrCreate();

                // Define the schema to ensure correct data types, especially for 'views'
                StructType schema = new StructType()
                                .add("video_id", DataTypes.StringType, true)
                                .add("trending_date", DataTypes.StringType, true) // Or DateType if you parse it
                                .add("title", DataTypes.StringType, true)
                                .add("channel_title", DataTypes.StringType, true)
                                .add("category_id", DataTypes.IntegerType, true) // Assuming category_id is integer
                                .add("publish_time", DataTypes.StringType, true) // Or TimestampType if you parse it
                                .add("tags", DataTypes.StringType, true)
                                .add("views", DataTypes.LongType, true) // Use LongType for views as it can be large
                                .add("likes", DataTypes.LongType, true)
                                .add("dislikes", DataTypes.LongType, true)
                                .add("comment_count", DataTypes.LongType, true)
                                .add("thumbnail_link", DataTypes.StringType, true)
                                .add("comments_disabled", DataTypes.BooleanType, true)
                                .add("ratings_disabled", DataTypes.BooleanType, true)
                                .add("video_error_or_removed", DataTypes.BooleanType, true)
                                .add("description", DataTypes.StringType, true);

                // Path to your CSV file
                String csvFilePath = "./output/Ziya_USVideos/proc.csv";

                // Read the CSV file
                Dataset<Row> videosDf = spark.read()
                                .option("header", "true") // Assumes the first line is the header
                                .option("escape", "\"") // Handles quotes within fields, e.g., in 'tags' or
                                                        // 'description'
                                .schema(schema) // Apply the defined schema
                                .csv(csvFilePath);

                // Task 2: Rank the most popular channels
                // Aggregate the number of views for each channel_title
                // Rank them according to the total number of views
                // Output the top ten channel_titles and their total views

                Dataset<Row> popularChannelsDf = videosDf
                                .groupBy("channel_title")
                                .agg(functions.sum("views").alias("total_views"))
                                .orderBy(functions.col("total_views").desc())
                                .limit(10); // Get the top 10

                // Show the results in a tabular format (optional, but good for quick view)
                System.out.println("Top 10 most popular channels by views (table format):");
                popularChannelsDf.show(false); // 'false' to prevent truncating column content

                // Print the results in the specified format: ("Channel Name", 123456)
                System.out.println("\nTop 10 most popular channels by views (custom format):");
                popularChannelsDf.foreach(row -> {
                        String channelTitle = row.getString(0); // First column is 'channel_title'
                        long totalViews = row.getLong(1); // Second column is 'total_views'
                        System.out.println("(\"" + channelTitle + "\"," + totalViews + ")");
                }); // This curly brace closes the lambda expression for foreach

                // Stop the SparkSession to release resources
                spark.stop();

        }

}