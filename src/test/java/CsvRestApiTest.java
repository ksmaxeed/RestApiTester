import com.fasterxml.jackson.databind.ObjectMapper;
import jp.moukin.CSVParser2;
import net.JavaNetHttpClient;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.cert.PKIXRevocationChecker.Option;
import java.util.*;
import java.util.function.Function;

import static net.JavaNetHttpClient.ResKey.CONTENTS;
import static net.JavaNetHttpClient.ResKey.STATUS;
import static org.junit.Assert.*;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Created by arch on 16/07/15.
 */
public abstract class CsvRestApiTest {

	static Collection<Object[]> getDatas(CSVParser2 parser) {
		List<Object[]> datas = new ArrayList<>();
		try {
			parser.parse(l -> {
				Object[] dataAry = new String[] { "", "", "", "", "", "" };
				System.arraycopy(l.toArray(new Object[l.size()]), 0, dataAry, 0, l.size());

				datas.add(dataAry);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return datas;
	}

	static Collection<Object[]> getDatas2(final String excelFilePath) {
		List<Object[]> datas = new ArrayList<>();
		try {
//	        Workbook workbook = new Workbook();
			Workbook wb = WorkbookFactory.create(new FileInputStream(excelFilePath));
			for (int i0 = 0, l0 = wb.getNumberOfSheets(); i0 < l0; i0++) {
				Sheet sheet = wb.getSheetAt(i0);
				System.out.println();
				for (Iterator<Row> it = sheet.rowIterator(); it.hasNext();) {
					Object[] dataAry = new String[] { "", "", "", "", "", "" };
					Row row = it.next();

					Function<Cell, String> mapper = c -> {
						switch (c.getCellType()) {
						case STRING:
							return c.getStringCellValue();
						case NUMERIC:
							String num = BigDecimal.valueOf(c.getNumericCellValue()).toPlainString();
							if (num.endsWith(".0")) {
								num = num.substring(0, num.length() - 2);
							}
							return num;
						default:
							break;
						}
						return "";
					};

					dataAry[0] = Optional.ofNullable(row.getCell(0)).map(mapper).orElse("");
					dataAry[1] = Optional.ofNullable(row.getCell(1)).map(mapper).orElse("");
					dataAry[2] = Optional.ofNullable(row.getCell(2)).map(mapper).orElse("");
					dataAry[3] = Optional.ofNullable(row.getCell(3)).map(mapper).orElse("");
					dataAry[4] = Optional.ofNullable(row.getCell(4)).map(mapper).orElse("");
					dataAry[5] = Optional.ofNullable(row.getCell(5)).map(mapper).orElse("");

					datas.add(dataAry);
				}

			}

		} catch (EncryptedDocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return datas;
	}

	@Parameterized.Parameter(value = 0)
	public String a0_method;

	@Parameterized.Parameter(value = 1)
	public String a1_url;

	@Parameterized.Parameter(value = 2)
	public String a2_post_data;

	@Parameterized.Parameter(value = 3)
	public String a3_response_status;

	@Parameterized.Parameter(value = 4)
	public String a4_response_type;

	@Parameterized.Parameter(value = 5)
	public String a5_response_data;

	private static Map<String, Object> globalValues = new HashMap<>();

	@Test
	public void runEachRecords() {
		String n_method = a0_method.trim().toUpperCase();
		String n_url = a1_url;
		String n_res_status = a3_response_status.trim();
		String n_data = a2_post_data;
		String n_res_type = a4_response_type.trim().toUpperCase();
		String n_response = a5_response_data;

		for (String key : globalValues.keySet()) {
			n_url = n_url.replaceAll("\\$" + key, globalValues.get(key).toString());
			n_data = n_data.replaceAll("\\$" + key, globalValues.get(key).toString());
			n_response = n_response.replaceAll("\\$" + key, globalValues.get(key).toString());
		}
		Map<JavaNetHttpClient.ResKey, String> response = null;

		try {
			switch (n_method) {
			case "POST":
				System.out.println("url = " + n_url);
				response = JavaNetHttpClient.executePost(n_url, n_data);
				break;

			case "GET":
				System.out.println("url = " + n_url);
				response = JavaNetHttpClient.executeGet(n_url);
				break;

			default:
				System.out.println(n_method + ":" + a1_url);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert response != null;

		assertEquals(n_res_status.trim(), response.get(STATUS));

		switch (n_res_type) {
		case "JSON":
			Map ext = jsonParse(n_response);
			Map act = jsonParse(response.get(CONTENTS));

			mapEquals(ext, act);
			break;

		case "SHOW":
			System.out.println("------> show response ------");
			System.out.println(response.get(STATUS));
			System.out.println(response.get(CONTENTS));
			System.out.println("<------");
		}

	}

	private static Map jsonParse(String jsonStr) {

		ObjectMapper mapper = new ObjectMapper();
		Map result = null;
		try {
			result = mapper.readValue(jsonStr, Map.class);
		} catch (IOException e) {
			System.out.println("Error parsing Json = " + e.getLocalizedMessage());
		}

		return result;
	}

	private void mapEquals(Map ext, Map act) {
		for (Object key : ext.keySet()) {
			Object e = ext.get(key);
			Object a = act.get(key);

			compareMapOrList(key, e, a);
		}
	}

	private void listEquals(Object key, List ext, List act) {
		try {

			for (int i = 0; i < ext.size(); i++) {
				Object e = ext.get(i);
				Object a = act.get(i);
				compareMapOrList(key, e, a);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			fail(e.getMessage());
		}
	}

	private void compareMapOrList(Object key, Object e, Object a) {
		if (e instanceof Map) {
			mapEquals((Map) e, (Map) a);
		} else if (e instanceof List) {
			listEquals(key, (List) e, (List) a);
		} else {
			String ee = e.toString();
			if (ee.startsWith("$")) {
				String gKey = ee.substring(1);
				globalValues.put(gKey, a);
				System.out.println("Input key:value -> " + gKey + ":" + a);
			} else {
				// System.out.println("Compare -> " + e + " : " + a);
				if (a == null) {
					System.out.println("key : e.toString() = " + key + ":" + e.toString());
				}
				assertNotNull(a);
				if (!Objects.equals(a.toString(), e.toString())) {
					System.out.println("key = " + key);
				}
				assertEquals(e.toString(), a.toString());
			}
		}
	}
}
