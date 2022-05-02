import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductTestMS {
    private static final Logger log = Logger.getLogger(ProductTestMS.class.getName());

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        int testPassed = 0;
        List<Method> methods = Arrays.stream(ProductTestMS.class.getMethods()).toList();
        List<Method> testList = methods.stream()
                .filter(x -> x.getAnnotation(ShouldRunAsTest.class) != null).toList();
        for (Method method : testList) {
            boolean testOutput = (Boolean) method.invoke(null);
            if (testOutput) testPassed++;
        }
        String output = String.format("Final Result: %s/%s", testPassed, testList.size());
        log.log(Level.INFO, output);
    }

    @ShouldRunAsTest
    public static boolean checkProductMSAvailablity() throws IOException {
        String messageBody = getRequest("http://localhost:8080/api/v1/product/status");
        if (messageBody.contains("ProductMS is Running")) {
            log.log(Level.INFO, "CheckProductMSAvailablity Passed");
            return true;
        } else {
            log.log(Level.SEVERE, "CheckProductMSAvailablity Failed");
            return false;
        }
    }

    @ShouldRunAsTest
    public static boolean getAllProducts() throws IOException {
        String messageBody = getRequest("http://localhost:8080/api/v1/product");
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<Product> product = objectMapper.readValue(messageBody, typeFactory.constructCollectionType(List.class, Product.class));
        // TODO: Can improve here, maybe use Design Pattern : Iterator Pattern to check each conditions individually??
        Product product1 = product.get(0);
        if (product1.getProductId().equals(256)
                && product1.getProductCost().equals(500.0)
                && product1.getProductName().equals("A Water Bottle")
        ){
            log.log(Level.INFO, "getAllProducts Passed");
            return true;
        } else {
            log.log(Level.SEVERE, "getAllProducts Failed");
            return false;
        }
    }

    private static String getRequest(String url) throws IOException {
        //set URL and open the connection
        URL productMSStatusUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) productMSStatusUrl.openConnection();

        //set Request headers
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(false);
        connection.connect();

        //get Response Body
        InputStream inputStream = connection.getInputStream();
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isReader);
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            content.append(inputLine);
        }
        bufferedReader.close();
        return content.toString();
    }
}
