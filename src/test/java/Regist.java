import jp.moukin.CSVParser2;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Collection;

/**
 * Created by kota.saito on 2016/03/08.
 */

@RunWith(Parameterized.class)
public class Regist extends CsvRestApiTest {

  @Parameters
  public static Collection<Object[]> data() {

    CSVParser2 parser = new CSVParser2(new File("src/test/resources/regist.csv"), "UTF-8");
    String excelPath = "src/test/resources/sample.xlsx";
    //System.out.println(getDatas2(excelPath));
    return getDatas2(excelPath);
  }

}