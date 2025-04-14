package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";
	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";
	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// 定義ファイルの正規表現
	private static final String REGULAR_BRANCH = "^[0-9]{3}+$";
	private static final String REGULAR_COMMODITY = "^[A-Za-z0-9]{8}+$";

	// 正規表現
	private static final String FILE_TYPE_BRANCH = "支店定義ファイル";
	private static final String FILE_TYPE_COMMODITY = "商品定義ファイル";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "が存在しません";
	private static final String FILE_INVALID_FORMAT = "のフォーマットが不正です";
	private static final String FILE_NOT_SERNO = "売上ファイル名が連番になっていません";
	private static final String TOTAL_OVER_10DIGIT = "合計金額が10桁を超えました";
	private static final String BRANCH_CODE_ERROR = "の支店コードが不正です";
	private static final String COMMODITY_CODE_ERROR = "の商品コードが不正です";
	private static final String FILE_FORMAT_ERROR = "のフォーマットが不正です";

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
		// 商品コードと商品名を保持するMap
		Map<String, String> commoditNames = new HashMap<>();
		// 商品コードと売上額を保持するMap
		Map<String, Long> commoditSales = new HashMap<>();

		// コマンドライン引数が1つ設定されているか
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店定義ファイルの読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, FILE_TYPE_BRANCH,
				REGULAR_BRANCH)) {
			return;
		}

		// 商品定義ファイルの読み込み処理
		if (!readFile(args[0], FILE_NAME_COMMODITY_LST, commoditNames, commoditSales, FILE_TYPE_COMMODITY,
				REGULAR_COMMODITY)) {
			return;
		}

		// 指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納（処理内容2-1）
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		// フォルダの格納情報から売上ファイルを探す（処理内容2-1）
		for (int i = 0; i < files.length; i++) {
			// getName() でファイル名を獲得し、8桁かつ.rcdのファイルのみ保持（処理内容2-1）
			if (files[i].isFile() && files[i].getName().matches("^\\d{8}[.]rcd$")) {

				rcdFiles.add(files[i]);
			}
		}
		// 売上ファイルを昇順にソート
		Collections.sort(rcdFiles);

		// 売上ファイルが連番か確認
		for (int i = 0; i < rcdFiles.size() - 1; i++) {
			// 比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換
			//			int former = Integer.parseInt((files[i].getName()).substring(0, 8));
			//			int latter = Integer.parseInt((files[i + 1]).getName().substring(0, 8));
			int former = Integer.parseInt(((rcdFiles.get(i)).getName()).substring(0, 8));
			int latter = Integer.parseInt(((rcdFiles.get(i + 1)).getName()).substring(0, 8));

			// 2つのファイル名の数字を比較
			if ((latter - former) != 1) {
				System.out.println(FILE_NOT_SERNO);
				return;
			}
		}

		// rcdFilesから売上ファイルの情報を取得（処理内容2-2）
		for (int i = 0; i < rcdFiles.size(); i++) {
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

				// 売上ファイルの中身が3行以外の場合はエラー
				if (rcdFilesSub.size() != 3) {
					System.out.println(file.getName() + FILE_FORMAT_ERROR);
					return;
				}

				// 支店コードが存在しない場合はエラー
				if (!branchNames.containsKey(rcdFilesSub.get(0))) {
					System.out.println(file.getName() + BRANCH_CODE_ERROR);
					return;
				}

				// 商品コードが存在しない場合はエラー
				if (!commoditNames.containsKey(rcdFilesSub.get(1))) {
					System.out.println(file.getName() + COMMODITY_CODE_ERROR);
					return;
				}

				// 売上金額が数字ではない場合はエラー
				if (!rcdFilesSub.get(2).matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				// 売上金額の型を変換
				long fileSale = Long.parseLong(rcdFilesSub.get(2));
				// 支店コードごとに売上金額を加算
				Long branchSaleAmount = branchSales.get(rcdFilesSub.get(0)) + fileSale;
				// 商品コードごとに合計金額を加算
				Long comSaleAmount = commoditSales.get(rcdFilesSub.get(1)) + fileSale;

				if ((branchSaleAmount >= 10000000000L) || (comSaleAmount >= 10000000000L)) {
					System.out.println(TOTAL_OVER_10DIGIT);
					return;
				}

				//加算した売上金額をMapに追加
				branchSales.put(rcdFilesSub.get(0), branchSaleAmount);
				commoditSales.put(rcdFilesSub.get(1), comSaleAmount);
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
		// 商品別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commoditNames, commoditSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル or 商品定義ファイルの読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名（商品コードと商品名）を保持するMap
	 * @param 支店コードと売上金額（商品コードと売上金額）を保持するMap
	 * @param 定義ファイルの種類
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> names,
			Map<String, Long> sales, String fileType, String regular) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			// 定義ファイルが存在しない場合
			if (!file.exists()) {
				System.out.println(fileType + FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;

			// 定義ファイルを一行ずつ読み込む(処理内容1-2)
			while ((line = br.readLine()) != null) {
				//支店（商品）コードと支店（商品）名をそれぞれ別に保持する為、文字列を分割する(処理内容1-2)
				String[] items = line.split(",");

				// 要素が「,」で区切られてない場合はエラー
				// 1つ目の要素が「支店定義の場合は数字3桁」、「商品定義の場合はアルファベットと数字8桁」でなければエラー
				if ((items.length != 2) || (!items[0].matches(regular))) {
					System.out.println(fileType + FILE_INVALID_FORMAT);
					return false;
				}

				// 処理内容：Mapに格納
				names.put(items[0], items[1]);
				// この時点では集計前の為、売上金額は0で固定(処理内容1-2)
				sales.put(items[0], 0L);
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
	 * @param 支店コードと支店名（商品コードと商品名）を保持するMap
	 * @param 支店コードと支店名（商品コードと売上金額）を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> names,
			Map<String, Long> sales) {

		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			// MapからKeyを取得(処理内容3-1)
			for (String key : names.keySet()) {
				// keyとkeyに対応するvalueをファイルに書き込む(処理内容3-1)
				bw.write(key + "," + names.get(key) + "," + sales.get(key));
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
