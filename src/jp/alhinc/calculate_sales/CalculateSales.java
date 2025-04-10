package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納（処理内容2-1）
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		//フォルダの格納情報から売上ファイルを探す（処理内容2-1）
		for (int i = 0; i < files.length; i++) {

			//files[i].getName() でファイル名を獲得し、8桁かつ.rcdのファイルを保持（処理内容2-1）
			if (files[i].getName().matches("^\\d{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		// rcdFilesから売上ファイルの情報を取得（処理内容2-2）
		for (int i = 0; i < rcdFiles.size(); i++) {
			////(処理内容2-2 ↓)
			BufferedReader br = null;

			try {
				File file = rcdFiles.get(i);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;
				List<String> rcdFilesSub = new ArrayList<>();

				// 売上ファイルを一行ずつ読み込む
				while ((line = br.readLine()) != null) {
					// 1行ずつ格納
					rcdFilesSub.add(line);
				}

				// Mapに加算していく為、売上金額の型を変換
				long fileSale = Long.parseLong(rcdFilesSub.get(1));
				// 支店コードごとに売上金額を加算
				Long saleAmount = branchSales.get(rcdFilesSub.get(0)) + fileSale;
				//加算した売上金額をMapに追加
				branchSales.put(rcdFilesSub.get(0), saleAmount);
			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);

			// 支店定義ファイルが存在しない場合
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			br = new BufferedReader(fr);
			String line;

			// branch.lstを一行ずつ読み込む(処理内容1-2)
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//支店コードと支店名をそれぞれ別に保持する為、文字列を分割する(処理内容1-2)
				String[] items = line.split(",");

				// 処理内容：Map（branchNames、branchSales）に格納(処理内容1-2)
				branchNames.put(items[0], items[1]);
				// この時点では集計前の為、売上金額は0で固定(処理内容1-2)
				branchSales.put(items[0], 0L);

				// 支店定義ファイルの仕様が異なる場合はエラー
				if ((items.length != 2) || (!items[0].matches("^\\d{3}[.]rcd$"))) {
					System.out.println(FILE_INVALID_FORMAT);
				}
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			// MapからKey(支店コード)を取得(処理内容3-1)
			for (String key : branchNames.keySet()) {
				// keyとkeyに対応するvalueをファイルに書き込む(処理内容3-1)
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
