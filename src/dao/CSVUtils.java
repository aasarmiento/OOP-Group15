package dao;

public class CSVUtils {
    
public static String[] splitCSVLine(String line) {
    // This regex splits by comma but is 'smart' enough to handle 
    // fields that might be quoted or contains extra spaces.
    // If you remove the quotes from the CSV, this ensures 
    // we can still reconstruct the address if it gets split.
    return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
}

    public static String clean(String s) {
        return (s == null) ? "" : s.trim().replace("\"", "");
    }

   public static double parseCurrency(String val) {
    if (val == null || val.trim().isEmpty() || val.equalsIgnoreCase("N/A")) return 0.0;
    return Double.parseDouble(val.replace(",", "").trim());
}
}