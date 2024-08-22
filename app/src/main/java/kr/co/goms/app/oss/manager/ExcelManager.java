package kr.co.goms.app.oss.manager;

import static org.apache.poi.ss.usermodel.Font.U_DOUBLE;
import static org.apache.poi.ss.usermodel.Font.U_NONE;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.app.oss.common.ManHolePrefs;
import kr.co.goms.app.oss.model.FieldBasicBeanS;
import kr.co.goms.app.oss.model.FieldDetailBeanS;
import kr.co.goms.app.oss.model.FieldDetailListSumBeanS;
import kr.co.goms.module.common.util.FileUtil;
import kr.co.goms.module.common.util.GomsLog;
import kr.co.goms.module.common.util.ImageUtil;
import kr.co.goms.module.common.util.StringUtil;

/**
 * 사용법

 */
public class ExcelManager {

    private static final String LOG_TAG = ExcelManager.class.getSimpleName();
    static ExcelManager mExcelManager;    //instance
    static Activity mActivity;
    HSSFWorkbook mWorkbook;

    HSSFSheet mSheet;           //현장조사야장
    HSSFCellStyle mHSSFCellStyleMainTitle;           //셀스타일
    HSSFCellStyle mHSSFCellStyleBorderRight;//셀스타일-border right
    HSSFCellStyle mHSSFCellStyleBorderLeftRight;//셀스타일-border left, right
    HSSFCellStyle mHSSFCellStyleBorderTopRight;//셀스타일-border top, right
    HSSFCellStyle mHSSFCellStyleBorderAll;      //셀스타일-border all
    HSSFFont mSumFont, mFont, mFontDataList, mFontReport;
    Row mRow;
    public enum SHEET_TYPE {
        SUMMARY,
        SUMMARY_DETAIL,
        SUMMARY_EXTENSION,
        DETAIL,
    }

    public enum CELL_TYPE_BG{
        Y,
        N
    }

    public enum HEIGHT_SIZE{
        MAIN((short)600),   //메인 타이틀 높이
        TITLE((short)360), //일반 높이
        VALUE((short)360),
        DIV((short)140),    //간격 높이
        EXTENTION((short) 400),//연장집계표 행 높이
        REPORT_ROW((short) 400),//현장조사야장 행 높이
        BLOCK_ROW((short) 300),//현장조사야장 > 블록 행 높이
        ;

        short size;
        HEIGHT_SIZE(short i) {
            this.size = i;
        }
    }

    public enum TITLE_SIZE{
        MAIN((short)400),
        SUM_TITLE((short)240),
        TITLE((short)140),
        VALUE((short)140),
        ;

        short size;
        TITLE_SIZE(short i) {
            this.size = i;
        }
    }

    enum CELL_STYLE {
        MAIN_TITLE(CELL_TYPE_BG.N, "현장 조사 야장", 0, 0, 0, 0, 0, 19, HEIGHT_SIZE.MAIN.size, (short) 400, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_DOUBLE),
        DIV_1(CELL_TYPE_BG.N, "", 1, 0, 1, 1, 0, 19, (short) 10, (short) 0, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        REPORTER_SPACE(CELL_TYPE_BG.N,"", 2, 0, 2, 2, 0, 14, HEIGHT_SIZE.TITLE.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        REPORTER(CELL_TYPE_BG.N,"조사자", 2, 15, 2, 2, 15, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_NO(CELL_TYPE_BG.Y,"NO.", 3, 0, 3, 3, 0, 0, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LOCAL(CELL_TYPE_BG.Y,"처리구역", 3, 1, 3, 3, 1, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_DRAINAGE(CELL_TYPE_BG.Y,"배제방식", 3, 3, 3, 3, 3, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_NUM(CELL_TYPE_BG.Y,"현장번호", 3, 5, 3, 3, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_STANDARD(CELL_TYPE_BG.Y,"규격", 3, 8, 3, 3, 8, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_DEPTH(CELL_TYPE_BG.Y,"현장깊이(m)", 3, 10, 3, 3, 10, 11, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LAT(CELL_TYPE_BG.Y,"GPS위도", 3, 12, 3, 3, 12, 14, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LNG(CELL_TYPE_BG.Y,"GPS경도", 3, 15, 3, 3, 15, 17, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_DATE(CELL_TYPE_BG.Y,"조사일시", 3, 18, 3, 3, 18, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_TITLE_NO(CELL_TYPE_BG.N,"", 4, 0, 4, 4, 0, 0, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_LOCAL(CELL_TYPE_BG.N, "", 4, 1, 4, 4, 1, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_DRAINAGE(CELL_TYPE_BG.N, "", 4, 3, 4, 4, 3, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_NUM(CELL_TYPE_BG.N, "", 4, 5, 4, 4, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_STANDARD(CELL_TYPE_BG.N, "", 4, 8, 4, 4, 8, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_DEPTH(CELL_TYPE_BG.N, "", 4, 10, 4, 4, 10, 11, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_LAT(CELL_TYPE_BG.N, "", 4, 12, 4, 4, 12, 14, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_LNG(CELL_TYPE_BG.N, "", 4, 15, 4, 4, 15, 17, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_DATE(CELL_TYPE_BG.N, "", 4, 18, 4, 4, 18, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_INSPECTION(CELL_TYPE_BG.Y, "점검내용", 5, 0, 5, 5, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_CAD(CELL_TYPE_BG.Y, "도면", 5, 10, 5, 5, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        HEAD_TITLE_MH_LID(CELL_TYPE_BG.Y,"현장뚜껑", 6, 0, 6, 6, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LIB_DAMAGE(CELL_TYPE_BG.N,"파손", 6, 3, 6, 6, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LIB_DAMAGE(CELL_TYPE_BG.N,"", 6, 4, 6, 6, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_LIB_CRACK(CELL_TYPE_BG.N,"균열", 6, 5, 6, 6, 5, 6, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LIB_CRACK(CELL_TYPE_BG.N,"", 6, 7, 6, 6, 7, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LIB_WATER(CELL_TYPE_BG.Y,"유입수", 6, 8, 6, 6, 8, 8, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LIB_WATER(CELL_TYPE_BG.N,"", 6, 9, 6, 6, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_TITLE_MH_CAD(CELL_TYPE_BG.N,"", 6, 10, 6, 14, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_OUTER(CELL_TYPE_BG.Y,"현장주변부", 7, 0, 7, 7, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_OUTER_DAMAGE(CELL_TYPE_BG.N,"파손", 7, 3, 7, 7, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_OUTER_DAMAGE(CELL_TYPE_BG.N,"", 7, 4, 7, 7, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_OUTER_CRACK(CELL_TYPE_BG.N,"균열", 7, 5, 7, 7, 5, 6, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_OUTER_CRACK(CELL_TYPE_BG.N,"", 7, 7, 7, 7, 7, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_OUTER_WATER(CELL_TYPE_BG.Y,"침입수등급(대중소)", 7, 8, 7, 9, 8, 8, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_OUTER_WATER(CELL_TYPE_BG.N,"", 7, 9, 7, 7, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_INNER(CELL_TYPE_BG.Y,"현장내부", 8, 0, 8, 8, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_INNER_DAMAGE(CELL_TYPE_BG.N,"파손", 8, 3, 8, 8, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_INNER_DAMAGE(CELL_TYPE_BG.N,"", 8, 4, 8, 8, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_INNER_CRACK(CELL_TYPE_BG.N,"균열", 8, 5, 8, 8, 5, 6, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_INNER_CRACK(CELL_TYPE_BG.N,"", 8, 7, 8, 8, 7, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_INNER_WATER(CELL_TYPE_BG.N,"", 8, 9, 8, 8, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),


        HEAD_TITLE_MH_PIPE(CELL_TYPE_BG.Y,"관로접합부", 9, 0, 9, 9, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_PIPE_DAMAGE(CELL_TYPE_BG.N,"파손", 9, 3, 9, 9, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_PIPE_DAMAGE(CELL_TYPE_BG.N,"", 9, 4, 9, 9, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_PIPE_CRACK(CELL_TYPE_BG.N,"균열", 9, 5, 9, 9, 5, 6, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_PIPE_CRACK(CELL_TYPE_BG.N,"", 9, 7, 9, 9, 7, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_PIPE_WATER(CELL_TYPE_BG.N,"", 9, 9, 9, 9, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_LADDER(CELL_TYPE_BG.Y,"사다리", 10, 0, 10, 10, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LADDER_YN(CELL_TYPE_BG.N,"유/무", 10, 3, 10, 11, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LADDER_YN(CELL_TYPE_BG.N,"", 10, 4, 10, 10, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_LADDER_DAMAGE(CELL_TYPE_BG.Y,"사다리손상", 10, 5, 10, 10, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LADDER_DAMAGE_LMS(CELL_TYPE_BG.N,"대중소", 10, 8, 10, 14, 8, 8, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LADDER_DAMAGE_LMS(CELL_TYPE_BG.N,"", 10, 9, 10, 10, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_INVERT(CELL_TYPE_BG.Y,"사다리", 11, 0, 11, 11, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_INVERT_YN(CELL_TYPE_BG.N,"", 11, 4, 11, 11, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_ENDOTHELIUM(CELL_TYPE_BG.Y,"사다리손상", 11, 5, 11, 11, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_ENDOTHELIUM_LMS(CELL_TYPE_BG.N,"", 11, 9, 11, 11, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),


        HEAD_TITLE_MH_ODOR(CELL_TYPE_BG.Y,"악취발생", 12, 0, 12, 12, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_ODOR_LMS(CELL_TYPE_BG.N,"양호,대중소", 12, 3, 12, 12, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 80, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_ODOR_YN(CELL_TYPE_BG.N,"", 12, 4, 12, 12, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_WASTEOIL_LMS(CELL_TYPE_BG.Y,"폐유부착", 12, 5, 12, 12, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_WASTEOIL_LMS(CELL_TYPE_BG.N,"", 12, 9, 12, 12, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_LID_SEALING(CELL_TYPE_BG.Y,"뚜껑밀페\n(개폐불가)", 13, 0, 13, 14, 0, 2, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LID_SEALING_YN(CELL_TYPE_BG.N,"여/부", 13, 3, 13, 14, 3, 3, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LID_SEALING_YN(CELL_TYPE_BG.N,"", 13, 4, 13, 14, 4, 4, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_TEMP_OBSTACLE_LMS(CELL_TYPE_BG.Y,"임시장애물", 13, 5, 13, 13, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_TEMP_OBSTACLE_LMS(CELL_TYPE_BG.N,"", 13, 9, 13, 13, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_ROOT_INTRUSION_LMS(CELL_TYPE_BG.Y,"뿌리 침입", 14, 5, 14, 14, 5, 7, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_ROOT_INTRUSION_LMS(CELL_TYPE_BG.N,"", 14, 9, 14, 14, 9, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_BLOCK_GAP(CELL_TYPE_BG.Y,"블록이음부단차", 15, 0, 15, 15, 0, 2, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_BLOCK_GAP_LMS(CELL_TYPE_BG.N,"대중소", 15, 3, 15, 18, 3, 3, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_BLOCK_GAP_LMS(CELL_TYPE_BG.N,"", 15, 4, 15, 15, 4, 4, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        SPACE_TITLE_MH_BLOCK_01(CELL_TYPE_BG.N,"", 15, 5, 15, 18, 5, 19, (short)300, (short)100, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_BLOCK_LEAVE(CELL_TYPE_BG.Y,"블록이음부이탈", 16, 0, 16, 16, 0, 2, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_BLOCK_LEAVE_LMS(CELL_TYPE_BG.N,"", 16, 4, 16, 16, 4, 4, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_BLOCK_DEMEGE(CELL_TYPE_BG.Y,"블록이음부손상", 17, 0, 17, 17, 0, 2, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_BLOCK_DEMEGE_LMS(CELL_TYPE_BG.N,"", 17, 4, 17, 17, 4, 4, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_SURFACE_GAP(CELL_TYPE_BG.Y,"표면단차", 18, 0, 18, 18, 0, 2, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_SURFACE_GAP_LMS(CELL_TYPE_BG.N,"", 18, 4, 18, 18, 4, 4, HEIGHT_SIZE.BLOCK_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        DIV_PHOTO(CELL_TYPE_BG.N,"", 19, 0, 19, 19, 0, 19, (short) 160, (short) 0, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        DIV_PHOTO_ELSE(CELL_TYPE_BG.N,"", 15, 0, 15, 15, 0, 19, (short) 200, (short) 0, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_PHOTO(CELL_TYPE_BG.Y,"사진대지", 20, 0, 20, 20, 0, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_PHOTO_ELSE(CELL_TYPE_BG.Y,"사진대지", 16, 0, 16, 16, 0, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_TITLE_PHOTO_AROUND_IMG(CELL_TYPE_BG.N,"", 21, 0, 21, 28, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_PHOTO_OUTER_IMG(CELL_TYPE_BG.N,"", 21, 10, 21, 28, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_TITLE_PHOTO_AROUND_IMG_ELSE(CELL_TYPE_BG.N,"", 17, 0, 17, 24, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_PHOTO_OUTER_IMG_ELSE(CELL_TYPE_BG.N,"", 17, 10, 17, 24, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),


        HEAD_TITLE_PHOTO_AROUND(CELL_TYPE_BG.Y,"현장전경", 29, 0, 29, 29, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_PHOTO_OUTER(CELL_TYPE_BG.Y,"현장외부", 29, 10, 29, 29, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        HEAD_TITLE_PHOTO_AROUND_ELSE(CELL_TYPE_BG.Y,"현장전경", 25, 0, 25, 25, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_PHOTO_OUTER_ELSE(CELL_TYPE_BG.Y,"현장외부", 25, 10, 25, 25, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),


        VALUE_TITLE_PHOTO_INNER_IMG(CELL_TYPE_BG.N,"", 30, 0, 30, 37, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_PHOTO_ETC_IMG(CELL_TYPE_BG.N,"", 30, 10, 30, 37, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_TITLE_PHOTO_INNER_IMG_ELSE(CELL_TYPE_BG.N,"", 26, 0, 26, 33, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_PHOTO_ETC_IMG_ELSE(CELL_TYPE_BG.N,"", 26, 10, 26, 33, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        HEAD_TITLE_PHOTO_INNER(CELL_TYPE_BG.Y,"현장내부", 38, 0, 38, 38, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_PHOTO_ETC(CELL_TYPE_BG.Y,"특이사항", 38, 10, 38, 38, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        HEAD_TITLE_PHOTO_INNER_ELSE(CELL_TYPE_BG.Y,"현장내부", 34, 0, 34, 34, 0, 9, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_PHOTO_ETC_ELSE(CELL_TYPE_BG.Y,"특이사항", 34, 10, 34, 34, 10, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        DIV_ETC(CELL_TYPE_BG.N,"", 39, 0, 39, 39, 0, 19, HEIGHT_SIZE.DIV.size, (short) 0, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        DIV_ETC_ELSE(CELL_TYPE_BG.N,"", 35, 0, 35, 35, 0, 19, HEIGHT_SIZE.DIV.size, (short) 0, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),


        HEAD_TITLE_ETC(CELL_TYPE_BG.Y,"특이사항", 40, 0, 40, 40, 0, 11, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LID_SIZE(CELL_TYPE_BG.Y,"뚜껑크기", 40, 12, 40, 40, 12, 13, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LID_SIZE(CELL_TYPE_BG.N,"", 40, 14, 40, 40, 14, 15, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_MH_MATERIAL(CELL_TYPE_BG.Y,"뚜껑재질", 40, 16, 40, 40, 16, 17, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_MATERIAL(CELL_TYPE_BG.N,"", 40, 18, 40, 40, 18, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        HEAD_TITLE_ETC_ELSE(CELL_TYPE_BG.Y,"특이사항", 36, 0, 36, 36, 0, 11, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        HEAD_TITLE_MH_LID_SIZE_ELSE(CELL_TYPE_BG.Y,"뚜껑크기", 36, 12, 36, 36, 12, 13, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_LID_SIZE_ELSE(CELL_TYPE_BG.N,"", 36, 14, 36, 36, 14, 15, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        HEAD_TITLE_MH_MATERIAL_ELSE(CELL_TYPE_BG.Y,"뚜껑재질", 36, 16, 36, 36, 16, 17, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_TITLE_MH_MATERIAL_ELSE(CELL_TYPE_BG.N,"", 36, 18, 36, 36, 18, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        VALUE_TITLE_ETC(CELL_TYPE_BG.N,"", 41, 0, 41, 45, 0, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_LEFT, CellStyle.VERTICAL_TOP, false, U_NONE),
        VALUE_TITLE_ETC_ELSE(CELL_TYPE_BG.N,"", 37, 0, 37, 41, 0, 19, HEIGHT_SIZE.REPORT_ROW.size, (short) 110, CellStyle.ALIGN_LEFT, CellStyle.VERTICAL_TOP, false, U_NONE),

        ;

        CELL_TYPE_BG cellTypeBg;
        String title;
        int row, col, firstRow, lastRow, firstCol, lastCol;
        short rowHeight, fontHeight, hAlignCenter, vAlignCenter;
        boolean bold;
        byte underline;
        CELL_STYLE(CELL_TYPE_BG cellTypeBg, String title, int row, int col, int firstRow, int lastRow, int firstCol, int lastCol, short rowHeight, short fontHeight, short hAlignCenter, short vAlignCenter, boolean bold, byte underline) {
            this.cellTypeBg = cellTypeBg;
            this.title = title;
            this.row=row;
            this.col = col;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.rowHeight = rowHeight;
            this.fontHeight = fontHeight;
            this.hAlignCenter = hAlignCenter;
            this.vAlignCenter = vAlignCenter;
            this.bold = bold;
            this.underline = underline;
        }
    }

    enum SUM_CELL_STYLE {
        MAIN_TITLE(CELL_TYPE_BG.N,"현장조사 총괄 집계표",0,0,0,0,0,77, HEIGHT_SIZE.MAIN.size, TITLE_SIZE.MAIN.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_DOUBLE),
        LOCAL(CELL_TYPE_BG.Y,"처리분구",1,0,1,4,0,0, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        COUNT(CELL_TYPE_BG.Y,"현장\n조사\n개수\n(개소)",1,1,1,4,1,1, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_YN(CELL_TYPE_BG.Y,"사다리설치",1,2,1,3,2,3, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        INVERT_YN(CELL_TYPE_BG.Y,"인버트설치",1,4,1,3,4,5, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM(CELL_TYPE_BG.Y,"이상항목",1,6,1,1,6,29, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION(CELL_TYPE_BG.Y,"침입수",1,30,1,1,30,38, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INNER(CELL_TYPE_BG.Y,"유입수",1,39,1,1,39,41, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        OBSTACLE(CELL_TYPE_BG.Y,"장애물",1,42,1,3,42,45, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ROOT_INTRUSION(CELL_TYPE_BG.Y,"뿌리침입",1,46,1,3,46,49, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ODOR(CELL_TYPE_BG.Y,"악취",1,50,1,3,50,53, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LID_SEALING(CELL_TYPE_BG.Y,"밀페",1,54,1,3,54,55, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BURIED(CELL_TYPE_BG.Y,"매몰",1,56,1,3,56,56, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_DAMAGE(CELL_TYPE_BG.Y,"사다리손상",1,57,1,3,57,59, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ENDOTHELIUM(CELL_TYPE_BG.Y,"내피생성",1,60,1,3,60,62, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WASTEOIL(CELL_TYPE_BG.Y,"폐유부착",1,63,1,3,63,65, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_GAP(CELL_TYPE_BG.Y,"블록이음부단차",1,66,1,3,66,68, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_DAMAGE(CELL_TYPE_BG.Y,"블록이음부손상",1,69,1,3,69,71, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_LEAVE(CELL_TYPE_BG.Y,"블록이음부이탈",1,72,1,3,72,74, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        SURFACE_GAP(CELL_TYPE_BG.Y,"표면단차",1,75,1,3,75,77, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LID(CELL_TYPE_BG.Y,"뚜껑",2,6,2,2,6,11, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_OUTER(CELL_TYPE_BG.Y,"현장주변부",2,12,2,2,12,17, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_INNER(CELL_TYPE_BG.Y,"현장내부",2,18,2,2,18,23, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_PIPE(CELL_TYPE_BG.Y,"관로접합부",2,24,2,2,24,29, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INVASION_OUTER(CELL_TYPE_BG.Y,"현장주변부",2,30,2,3,30,32, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INVASION_INNER(CELL_TYPE_BG.Y,"현장내부",2,33,2,3,33,35, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INVASION_PIPE(CELL_TYPE_BG.Y,"관로접합부",2,36,2,3,36,38, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INNER_LID(CELL_TYPE_BG.Y,"뚜껑",2,39,2,3,39,41, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_DAMAGE(CELL_TYPE_BG.Y,"파손",3,6,3,3,6,8, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_CRACK(CELL_TYPE_BG.Y,"균열",3,9,3,3,9,11, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_OUTER_DAMAGE(CELL_TYPE_BG.Y,"파손",3,12,3,3,12,14, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_OUTER_CRACK(CELL_TYPE_BG.Y,"균열",3,15,3,3,15,17, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_INNER_DAMAGE(CELL_TYPE_BG.Y,"파손",3,18,3,3,18,20, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_INNER_CRACK(CELL_TYPE_BG.Y,"균열",3,21,3,3,21,23, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_PIPE_DAMAGE(CELL_TYPE_BG.Y,"파손",3,24,3,3,24,26, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_PIPE_CRACK(CELL_TYPE_BG.Y,"균열",3,27,3,3,27,29, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_Y(CELL_TYPE_BG.N,"유",4,2,4,4,2,2, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_N(CELL_TYPE_BG.N,"무",4,3,4,4,3,3, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        INVERT_Y(CELL_TYPE_BG.N,"유",4,4,4,4,4,4, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        INVERT_N(CELL_TYPE_BG.N,"무",4,5,4,4,5,5, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_DAMAGE_L(CELL_TYPE_BG.N,"대",4,6,4,4,6,6, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_DAMAGE_M(CELL_TYPE_BG.N,"중",4,7,4,4,7,7, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_DAMAGE_S(CELL_TYPE_BG.N,"소",4,8,4,4,8,8, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_CRACK_L(CELL_TYPE_BG.N,"대",4,9,4,4,9,9, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_CRACK_M(CELL_TYPE_BG.N,"중",4,10,4,4,10,10, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_CRACK_S(CELL_TYPE_BG.N,"소",4,11,4,4,11,11, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_OUTER_DAMAGE_L(CELL_TYPE_BG.N,"대",4,12,4,4,12,12, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_OUTER_DAMAGE_M(CELL_TYPE_BG.N,"중",4,13,4,4,13,13, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_OUTER_DAMAGE_S(CELL_TYPE_BG.N,"소",4,14,4,4,14,14, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_OUTER_CRACK_L(CELL_TYPE_BG.N,"대",4,15,4,4,15,15, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_OUTER_CRACK_M(CELL_TYPE_BG.N,"중",4,16,4,4,16,16, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_OUTER_CRACK_S(CELL_TYPE_BG.N,"소",4,17,4,4,17,17, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_INNER_DAMAGE_L(CELL_TYPE_BG.N,"대",4,18,4,4,18,18, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_INNER_DAMAGE_M(CELL_TYPE_BG.N,"중",4,19,4,4,19,19, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_INNER_DAMAGE_S(CELL_TYPE_BG.N,"소",4,20,4,4,20,20, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_INNER_CRACK_L(CELL_TYPE_BG.N,"대",4,21,4,4,21,21, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_INNER_CRACK_M(CELL_TYPE_BG.N,"중",4,22,4,4,22,22, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_INNER_CRACK_S(CELL_TYPE_BG.N,"소",4,23,4,4,23,23, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_PIPE_DAMAGE_L(CELL_TYPE_BG.N,"대",4,24,4,4,24,24, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_PIPE_DAMAGE_M(CELL_TYPE_BG.N,"중",4,25,4,4,25,25, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_PIPE_DAMAGE_S(CELL_TYPE_BG.N,"소",4,26,4,4,26,26, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_PIPE_CRACK_L(CELL_TYPE_BG.N,"대",4,27,4,4,27,27, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_PIPE_CRACK_M(CELL_TYPE_BG.N,"중",4,28,4,4,28,28, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_PIPE_CRACK_S(CELL_TYPE_BG.N,"소",4,29,4,4,29,29, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_OUTER_L(CELL_TYPE_BG.N,"대",4,30,4,4,30,30, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_OUTER_M(CELL_TYPE_BG.N,"중",4,31,4,4,31,31, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_OUTER_S(CELL_TYPE_BG.N,"소",4,32,4,4,32,32, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_INNER_L(CELL_TYPE_BG.N,"대",4,33,4,4,33,33, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_INNER_M(CELL_TYPE_BG.N,"중",4,34,4,4,34,34, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_INNER_S(CELL_TYPE_BG.N,"소",4,35,4,4,35,35, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_PIPE_L(CELL_TYPE_BG.N,"대",4,36,4,4,36,36, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_PIPE_M(CELL_TYPE_BG.N,"중",4,37,4,4,37,37, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION_PIPE_S(CELL_TYPE_BG.N,"소",4,38,4,4,38,38, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INNER_MH_L(CELL_TYPE_BG.N,"대",4,39,4,4,39,39, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INNER_MH_M(CELL_TYPE_BG.N,"중",4,40,4,4,40,40, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INNER_MH_S(CELL_TYPE_BG.N,"소",4,41,4,4,41,41, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        OBSTACLE_G(CELL_TYPE_BG.N,"양호",4,42,4,4,42,42, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        OBSTACLE_L(CELL_TYPE_BG.N,"대",4,43,4,4,43,43, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        OBSTACLE_M(CELL_TYPE_BG.N,"중",4,44,4,4,44,44, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        OBSTACLE_S(CELL_TYPE_BG.N,"소",4,45,4,4,45,45, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ROOT_INTRUSION_G(CELL_TYPE_BG.N,"양호",4,46,4,4,46,46, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ROOT_INTRUSION_L(CELL_TYPE_BG.N,"대",4,47,4,4,47,47, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ROOT_INTRUSION_M(CELL_TYPE_BG.N,"중",4,48,4,4,48,48, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ROOT_INTRUSION_S(CELL_TYPE_BG.N,"소",4,49,4,4,49,49, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ODOR_G(CELL_TYPE_BG.N,"양호",4,50,4,4,50,50, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ODOR_L(CELL_TYPE_BG.N,"대",4,51,4,4,51,51, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ODOR_M(CELL_TYPE_BG.N,"중",4,52,4,4,52,52, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ODOR_S(CELL_TYPE_BG.N,"소",4,53,4,4,53,53, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LID_SAELING_Y(CELL_TYPE_BG.N,"여",4,54,4,4,54,54, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LID_SAELING_N(CELL_TYPE_BG.N,"부",4,55,4,4,55,55, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BURIED_Y(CELL_TYPE_BG.N,"유",4,56,4,4,56,56, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_DAMAGE_L(CELL_TYPE_BG.N,"대",4,57,4,4,57,57, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_DAMAGE_M(CELL_TYPE_BG.N,"중",4,58,4,4,58,58, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_DAMAGE_S(CELL_TYPE_BG.N,"소",4,59,4,4,59,59, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ENDOTHELIUM_L(CELL_TYPE_BG.N,"대",4,60,4,4,60,60, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ENDOTHELIUM_M(CELL_TYPE_BG.N,"중",4,61,4,4,61,61, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ENDOTHELIUM_S(CELL_TYPE_BG.N,"소",4,62,4,4,62,62, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WASTEOIL_L(CELL_TYPE_BG.N,"대",4,63,4,4,63,63, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WASTEOIL_M(CELL_TYPE_BG.N,"중",4,64,4,4,64,64, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WASTEOIL_S(CELL_TYPE_BG.N,"소",4,65,4,4,65,65, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_GAP_L(CELL_TYPE_BG.N,"대",4,66,4,4,66,66, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_GAP_M(CELL_TYPE_BG.N,"중",4,67,4,4,67,67, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_GAP_S(CELL_TYPE_BG.N,"소",4,68,4,4,68,68, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_DAMAGE_L(CELL_TYPE_BG.N,"대",4,69,4,4,69,69, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_DAMAGE_M(CELL_TYPE_BG.N,"중",4,70,4,4,70,70, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_DAMAGE_S(CELL_TYPE_BG.N,"소",4,71,4,4,71,71, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_LEAVE_L(CELL_TYPE_BG.N,"대",4,72,4,4,72,72, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_LEAVE_M(CELL_TYPE_BG.N,"중",4,73,4,4,73,73, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_LEAVE_S(CELL_TYPE_BG.N,"소",4,74,4,4,74,74, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        SURFACE_GAP_L(CELL_TYPE_BG.N,"대",4,75,4,4,75,75, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        SURFACE_GAP_M(CELL_TYPE_BG.N,"중",4,76,4,4,76,76, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        SURFACE_GAP_S(CELL_TYPE_BG.N,"소",4,77,4,4,77,77, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_LOCAL(CELL_TYPE_BG.N,"",5,0,5,5,0,0, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_COUNT(CELL_TYPE_BG.N,"",5,1,5,5,1,1, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LADDER_Y(CELL_TYPE_BG.N,"",5,2,5,5,2,2, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LADDER_N(CELL_TYPE_BG.N,"",5,3,5,5,3,3, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_INVERT_Y(CELL_TYPE_BG.N,"",5,4,5,5,4,4, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_INVERT_N(CELL_TYPE_BG.N,"",5,5,5,5,5,5, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE_L(CELL_TYPE_BG.N,"",5,6,5,5,6,6, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE_M(CELL_TYPE_BG.N,"",5,7,5,5,7,7, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE_S(CELL_TYPE_BG.N,"",5,8,5,5,8,8, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_CRACK_L(CELL_TYPE_BG.N,"",5,9,5,5,9,9, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_CRACK_M(CELL_TYPE_BG.N,"",5,10,5,5,10,10, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_CRACK_S(CELL_TYPE_BG.N,"",5,11,5,5,11,11, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_OUTER_DAMAGE_L(CELL_TYPE_BG.N,"",5,12,5,5,12,12, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_OUTER_DAMAGE_M(CELL_TYPE_BG.N,"",5,13,5,5,13,13, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_OUTER_DAMAGE_S(CELL_TYPE_BG.N,"",5,14,5,5,14,14, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_OUTER_CRACK_L(CELL_TYPE_BG.N,"",5,15,5,5,15,15, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_OUTER_CRACK_M(CELL_TYPE_BG.N,"",5,16,5,5,16,16, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_OUTER_CRACK_S(CELL_TYPE_BG.N,"",5,17,5,5,17,17, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_INNER_DAMAGE_L(CELL_TYPE_BG.N,"",5,18,5,5,18,18, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_INNER_DAMAGE_M(CELL_TYPE_BG.N,"",5,19,5,5,19,19, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_INNER_DAMAGE_S(CELL_TYPE_BG.N,"",5,20,5,5,20,20, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_INNER_CRACK_L(CELL_TYPE_BG.N,"",5,21,5,5,21,21, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_INNER_CRACK_M(CELL_TYPE_BG.N,"",5,22,5,5,22,22, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_INNER_CRACK_S(CELL_TYPE_BG.N,"",5,23,5,5,23,23, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_PIPE_DAMAGE_L(CELL_TYPE_BG.N,"",5,24,5,5,24,24, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_PIPE_DAMAGE_M(CELL_TYPE_BG.N,"",5,25,5,5,25,25, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_PIPE_DAMAGE_S(CELL_TYPE_BG.N,"",5,26,5,5,26,26, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_PIPE_CRACK_L(CELL_TYPE_BG.N,"",5,27,5,5,27,27, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_PIPE_CRACK_M(CELL_TYPE_BG.N,"",5,28,5,5,28,28, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_PIPE_CRACK_S(CELL_TYPE_BG.N,"",5,29,5,5,29,29, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_OUTER_L(CELL_TYPE_BG.N,"",5,30,5,5,30,30, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_OUTER_M(CELL_TYPE_BG.N,"",5,31,5,5,31,31, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_OUTER_S(CELL_TYPE_BG.N,"",5,32,5,5,32,32, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_INNER_L(CELL_TYPE_BG.N,"",5,33,5,5,33,33, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_INNER_M(CELL_TYPE_BG.N,"",5,34,5,5,34,34, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_INNER_S(CELL_TYPE_BG.N,"",5,35,5,5,35,35, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_PIPE_L(CELL_TYPE_BG.N,"",5,36,5,5,36,36, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_PIPE_M(CELL_TYPE_BG.N,"",5,37,5,5,37,37, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INVASION_PIPE_S(CELL_TYPE_BG.N,"",5,38,5,5,38,38, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INNER_MH_L(CELL_TYPE_BG.N,"",5,39,5,5,39,39, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INNER_MH_M(CELL_TYPE_BG.N,"",5,40,5,5,40,40, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WATER_INNER_MH_S(CELL_TYPE_BG.N,"",5,41,5,5,41,41, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_OBSTACLE_G(CELL_TYPE_BG.N,"",5,42,5,5,42,42, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_OBSTACLE_L(CELL_TYPE_BG.N,"",5,43,5,5,43,43, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_OBSTACLE_M(CELL_TYPE_BG.N,"",5,44,5,5,44,44, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_OBSTACLE_S(CELL_TYPE_BG.N,"",5,45,5,5,45,45, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ROOT_INTRUSION_G(CELL_TYPE_BG.N,"",5,46,5,5,46,46, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ROOT_INTRUSION_L(CELL_TYPE_BG.N,"",5,47,5,5,47,47, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ROOT_INTRUSION_M(CELL_TYPE_BG.N,"",5,48,5,5,48,48, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ROOT_INTRUSION_S(CELL_TYPE_BG.N,"",5,49,5,5,49,49, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ODOR_G(CELL_TYPE_BG.N,"",5,50,5,5,50,50, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ODOR_L(CELL_TYPE_BG.N,"",5,51,5,5,51,51, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ODOR_M(CELL_TYPE_BG.N,"",5,52,5,5,52,52, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ODOR_S(CELL_TYPE_BG.N,"",5,53,5,5,53,53, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LID_SAELING_Y(CELL_TYPE_BG.N,"",5,54,5,5,54,54, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LID_SAELING_N(CELL_TYPE_BG.N,"",5,55,5,5,55,55, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BURIED_Y(CELL_TYPE_BG.N,"",5,56,5,5,56,56, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LADDER_DAMAGE_L(CELL_TYPE_BG.N,"",5,57,5,5,57,57, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LADDER_DAMAGE_M(CELL_TYPE_BG.N,"",5,58,5,5,58,58, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_LADDER_DAMAGE_S(CELL_TYPE_BG.N,"",5,59,5,5,59,59, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ENDOTHELIUM_L(CELL_TYPE_BG.N,"",5,60,5,5,60,60, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ENDOTHELIUM_M(CELL_TYPE_BG.N,"",5,61,5,5,61,61, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_ENDOTHELIUM_S(CELL_TYPE_BG.N,"",5,62,5,5,62,62, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WASTEOIL_L(CELL_TYPE_BG.N,"",5,63,5,5,63,63, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WASTEOIL_M(CELL_TYPE_BG.N,"",5,64,5,5,64,64, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_WASTEOIL_S(CELL_TYPE_BG.N,"",5,65,5,5,65,65, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_GAP_L(CELL_TYPE_BG.N,"",5,66,5,5,66,66, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_GAP_M(CELL_TYPE_BG.N,"",5,67,5,5,67,67, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_GAP_S(CELL_TYPE_BG.N,"",5,68,5,5,68,68, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_DAMAGE_L(CELL_TYPE_BG.N,"",5,69,5,5,69,69, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_DAMAGE_M(CELL_TYPE_BG.N,"",5,70,5,5,70,70, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_DAMAGE_S(CELL_TYPE_BG.N,"",5,71,5,5,71,71, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_LEAVE_L(CELL_TYPE_BG.N,"",5,72,5,5,72,72, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_LEAVE_M(CELL_TYPE_BG.N,"",5,73,5,5,73,73, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_BLOCK_LEAVE_S(CELL_TYPE_BG.N,"",5,74,5,5,74,74, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_SURFACE_GAP_L(CELL_TYPE_BG.N,"",5,75,5,5,75,75, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_SURFACE_GAP_M(CELL_TYPE_BG.N,"",5,76,5,5,76,76, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        VALUE_SURFACE_GAP_S(CELL_TYPE_BG.N,"",5,77,5,5,77,77, HEIGHT_SIZE.VALUE.size, TITLE_SIZE.VALUE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        ;

        CELL_TYPE_BG cellTypeBg;
        String title;
        int row, col, firstRow, lastRow, firstCol, lastCol;
        short rowHeight, fontHeight, hAlignCenter, vAlignCenter;
        boolean bold;
        byte underline;
        SUM_CELL_STYLE(CELL_TYPE_BG cellTypeBg, String title, int row, int col, int firstRow, int lastRow, int firstCol, int lastCol, short rowHeight, short fontHeight, short hAlignCenter, short vAlignCenter, boolean bold, byte underline) {
            this.cellTypeBg = cellTypeBg;
            this.title = title;
            this.row=row;
            this.col = col;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.rowHeight = rowHeight;
            this.fontHeight = fontHeight;
            this.hAlignCenter = hAlignCenter;
            this.vAlignCenter = vAlignCenter;
            this.bold = bold;
            this.underline = underline;
        }
    }

    enum SUM_DETAIL_CELL_STYLE {
        SUM_DETAIL_MAIN_TITLE(CELL_TYPE_BG.N,"현장조사 집계표",0,0,0,0,0,30, HEIGHT_SIZE.MAIN.size, TITLE_SIZE.MAIN.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_DOUBLE),
        NO(CELL_TYPE_BG.Y,"야장\n쪽수",1,0,1,3,0,0, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LOCAL(CELL_TYPE_BG.Y,"처리\n분구",1,1,1,3,1,1, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_NUM(CELL_TYPE_BG.Y,"현장번호",1,2,1,3,2,2, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_DRAINAGE(CELL_TYPE_BG.Y,"배제방식",1,3,1,3,3,3, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_YN(CELL_TYPE_BG.Y,"사다리\n설치",1,4,1,3,4,4, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        INVERT_YN(CELL_TYPE_BG.Y,"인버트\n설치",1,5,1,3,5,5, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM(CELL_TYPE_BG.Y,"이상항목",1,6,1,1,6,13, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INVASION(CELL_TYPE_BG.Y,"침입수",1,14,1,1,14,16, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WATER_INNER(CELL_TYPE_BG.Y,"유입수",1,17,1,1,17,17, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        OBSTACLE(CELL_TYPE_BG.Y,"장애물",1,18,1,3,18,18, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ROOT_INTRUSION(CELL_TYPE_BG.Y,"뿌리침입",1,19,1,3,19,19, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ODOR(CELL_TYPE_BG.Y,"악취",1,20,1,3,20,20, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LID_SEALING(CELL_TYPE_BG.Y,"밀페",1,21,1,3,21,21, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BURIED(CELL_TYPE_BG.Y,"매몰",1,22,1,3,22,22, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LADDER_DAMAGE(CELL_TYPE_BG.Y,"사다리손상",1,23,1,3,23,23, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ENDOTHELIUM(CELL_TYPE_BG.Y,"내피생성",1,24,1,3,24,24, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        WASTEOIL(CELL_TYPE_BG.Y,"폐유부착",1,25,1,3,25,25, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_GAP(CELL_TYPE_BG.Y,"블록이음부\n단차",1,26,1,3,26,26, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_DAMAGE(CELL_TYPE_BG.Y,"블록이음부\n손상",1,27,1,3,27,27, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        BLOCK_LEAVE(CELL_TYPE_BG.Y,"블록이음부\n이탈",1,28,1,3,28,28, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        SURFACE_GAP(CELL_TYPE_BG.Y,"표면단차",1,29,1,3,29,29, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_REMARK(CELL_TYPE_BG.Y,"특이사항",1,30,1,3,30,30, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LID(CELL_TYPE_BG.Y,"뚜껑",2,6,2,2,6,7, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_OUTER(CELL_TYPE_BG.Y,"현장주변부",2,8,2,2,8,9, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_INNER(CELL_TYPE_BG.Y,"현장내부",2,10,2,2,10,11, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_PIPE(CELL_TYPE_BG.Y,"관로접합부",2,12,2,2,12,13, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INVASION_OUTER(CELL_TYPE_BG.Y,"현장주변부",2,14,2,3,14,14, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INVASION_INNER(CELL_TYPE_BG.Y,"현장내부",2,15,2,3,15,15, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INVASION_PIPE(CELL_TYPE_BG.Y,"관로접합부",2,16,2,3,16,16, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_WATER_INNER_LID(CELL_TYPE_BG.Y,"현장뚜껑",2,17,2,3,17,17, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_DAMAGE(CELL_TYPE_BG.Y,"파손",3,6,3,3,6,6, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_CRACK(CELL_TYPE_BG.Y,"균열",3,7,3,3,7,7, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_OUTER_DAMAGE(CELL_TYPE_BG.Y,"파손",3,8,3,3,8,8, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_OUTER_CRACK(CELL_TYPE_BG.Y,"균열",3,9,3,3,9,9, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_INNER_DAMAGE(CELL_TYPE_BG.Y,"파손",3,10,3,3,10,10, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_INNER_CRACK(CELL_TYPE_BG.Y,"균열",3,11,3,3,11,11, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_PIPE_DAMAGE(CELL_TYPE_BG.Y,"파손",3,12,3,3,12,12, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        ABNORMAL_ITEM_MH_LID_PIPE_CRACK(CELL_TYPE_BG.Y,"균열",3,13,3,3,13,13, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_NO(CELL_TYPE_BG.N,"",4,0,4,4,0,0, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_LOCAL(CELL_TYPE_BG.N,"",4,1,4,4,1,1, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_NUM(CELL_TYPE_BG.N,"",4,2,4,4,2,2, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_DRAINAGE(CELL_TYPE_BG.N,"",4,3,4,4,3,3, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_LADDER_YN(CELL_TYPE_BG.N,"",4,4,4,4,4,4, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_INVERT_YN(CELL_TYPE_BG.N,"",4,5,4,4,5,5, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE(CELL_TYPE_BG.N,"",4,6,4,4,6,6, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_CRACK(CELL_TYPE_BG.N,"",4,7,4,4,7,7, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_OUTER_DAMAGE(CELL_TYPE_BG.N,"",4,8,4,4,8,8, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_OUTER_CRACK(CELL_TYPE_BG.N,"",4,9,4,4,9,9, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_INNER_DAMAGE(CELL_TYPE_BG.N,"",4,10,4,4,10,10, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_INNER_CRACK(CELL_TYPE_BG.N,"",4,11,4,4,11,11, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_PIPE_DAMAGE(CELL_TYPE_BG.N,"",4,12,4,4,12,12, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ABNORMAL_ITEM_MH_LID_PIPE_CRACK(CELL_TYPE_BG.N,"",4,13,4,4,13,13, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_WATER_INVASION_OUTER(CELL_TYPE_BG.N,"",4,14,4,4,14,14, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_WATER_INVASION_INNER(CELL_TYPE_BG.N,"",4,15,4,4,15,15, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_WATER_INVASION_PIPE(CELL_TYPE_BG.N,"",4,16,4,4,16,16, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_WATER_INNER_LID(CELL_TYPE_BG.N,"",4,17,4,4,17,17, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_OBSTACLE(CELL_TYPE_BG.N,"",4,18,4,4,18,18, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ROOT_INTRUSION(CELL_TYPE_BG.N,"",4,19,4,4,19,19, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ODOR(CELL_TYPE_BG.N,"",4,20,4,4,20,20, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_LID_SEALING_YN(CELL_TYPE_BG.N,"",4,21,4,4,21,21, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_BURIED_YN(CELL_TYPE_BG.N,"",4,22,4,4,22,22, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_LADDER_DAMAGE(CELL_TYPE_BG.N,"",4,23,4,4,23,23, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_ENDOTHELIUM(CELL_TYPE_BG.N,"",4,24,4,4,24,24, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_WASTEOIL(CELL_TYPE_BG.N,"",4,25,4,4,25,25, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_BLOCK_GAP(CELL_TYPE_BG.N,"",4,26,4,4,26,26, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_BLOCK_DAMAGE(CELL_TYPE_BG.N,"",4,27,4,4,27,27, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_BLOCK_LEAVE(CELL_TYPE_BG.N,"",4,28,4,4,28,28, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_SURFACE_GAP(CELL_TYPE_BG.N,"",4,29,4,4,29,29, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_REMARK(CELL_TYPE_BG.N,"",4,30,4,4,30,30, HEIGHT_SIZE.TITLE.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        ;

        CELL_TYPE_BG cellTypeBg;
        String title;
        int row, col, firstRow, lastRow, firstCol, lastCol;
        short rowHeight, fontHeight, hAlignCenter, vAlignCenter;
        boolean bold;
        byte underline;
        SUM_DETAIL_CELL_STYLE(CELL_TYPE_BG cellTypeBg, String title, int row, int col, int firstRow, int lastRow, int firstCol, int lastCol, short rowHeight, short fontHeight, short hAlignCenter, short vAlignCenter, boolean bold, byte underline) {
            this.cellTypeBg = cellTypeBg;
            this.title = title;
            this.row=row;
            this.col = col;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.rowHeight = rowHeight;
            this.fontHeight = fontHeight;
            this.hAlignCenter = hAlignCenter;
            this.vAlignCenter = vAlignCenter;
            this.bold = bold;
            this.underline = underline;
        }
    }

    /**
     * 연장집계표 스타일
     */
    enum SUM_EXTENSION_CELL_STYLE{
        MAIN_TITLE(CELL_TYPE_BG.N,"연장집계표",0,0,0,0,0,9, HEIGHT_SIZE.MAIN.size, TITLE_SIZE.MAIN.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_DOUBLE),
        NO(CELL_TYPE_BG.Y,"연번",1,0,1,2,0,0, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_DATE(CELL_TYPE_BG.Y,"조사일",1,1,1,2,1,1, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        LOCAL(CELL_TYPE_BG.Y,"처리분구",1,2,1,2,2,2, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_NUM(CELL_TYPE_BG.Y,"현장번호",1,3,1,1,3,4, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_DRAINAGE(CELL_TYPE_BG.Y,"배제방식",1,5,1,2,5,5, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LOCAL_SPACIES(CELL_TYPE_BG.Y,"관종",1,6,1,2,6,6, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LOCAL_CIRCUMFERENCT(CELL_TYPE_BG.Y,"관경",1,7,1,2,7,7, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LOCAL_EXTENSION(CELL_TYPE_BG.Y,"연장",1,8,1,2,8,8, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LOCAL_BIGO(CELL_TYPE_BG.Y,"비고",1,9,1,2,9,9, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LOCAL_SP(CELL_TYPE_BG.Y,"시점",2,3,2,2,3,3, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),
        MH_LOCAL_EP(CELL_TYPE_BG.Y,"종점",2,4,2,2,4,4, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, true, U_NONE),

        VALUE_NO(CELL_TYPE_BG.N,"",3,0,3,3,0,0, HEIGHT_SIZE.MAIN.size, TITLE_SIZE.MAIN.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_DATE(CELL_TYPE_BG.N,"",3,1,3,3,1,1, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_LOCAL(CELL_TYPE_BG.N,"",3,2,3,3,2,2, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_LOCAL_SP(CELL_TYPE_BG.N,"",3,3,3,3,3,3, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_LOCAL_EP(CELL_TYPE_BG.N,"",3,4,3,3,4,4, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_DRAINAGE(CELL_TYPE_BG.N,"",3,5,3,3,5,5, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_LOCAL_SPACIES(CELL_TYPE_BG.N,"",3,6,3,3,6,6, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_LOCAL_CIRCUMFERENCE(CELL_TYPE_BG.N,"",3,7,3,3,7,7, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_LOCAL_EXTENTION(CELL_TYPE_BG.N,"",3,8,3,3,8,8, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),
        VALUE_MH_LOCAL_BIGO(CELL_TYPE_BG.N,"",3,9,3,3,9,9, HEIGHT_SIZE.EXTENTION.size, TITLE_SIZE.TITLE.size, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER, false, U_NONE),

        ;

        CELL_TYPE_BG cellTypeBg;
        String title;
        int row, col, firstRow, lastRow, firstCol, lastCol;
        short rowHeight, fontHeight, hAlignCenter, vAlignCenter;
        boolean bold;
        byte underline;
        SUM_EXTENSION_CELL_STYLE(CELL_TYPE_BG cellTypeBg, String title, int row, int col, int firstRow, int lastRow, int firstCol, int lastCol, short rowHeight, short fontHeight, short hAlignCenter, short vAlignCenter, boolean bold, byte underline) {
            this.cellTypeBg = cellTypeBg;
            this.title = title;
            this.row=row;
            this.col = col;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.rowHeight = rowHeight;
            this.fontHeight = fontHeight;
            this.hAlignCenter = hAlignCenter;
            this.vAlignCenter = vAlignCenter;
            this.bold = bold;
            this.underline = underline;
        }

    }
    public ExcelManager() {
    }

    public static ExcelManager I(Activity activity){
        if(mExcelManager == null){
            mExcelManager = new ExcelManager();
        }

/*        if(mWorkbook != null){
            try {
                mWorkbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mWorkbook = createWorkbook();*/
        mActivity = activity;
        return mExcelManager;
    }

    public HSSFWorkbook createWorkbook() {

        if(mWorkbook != null){
            try {
                mWorkbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

/*        if(mWorkbook == null) {
            mWorkbook = new HSSFWorkbook();
        }*/

        HSSFWorkbook workbook = new HSSFWorkbook();
        mWorkbook = workbook;
        return workbook;
    }

    public HSSFSheet createSheet(String sheetName){
        if(mSheet == null) {
            mSheet = mWorkbook.createSheet(sheetName);
        }
        return mSheet;
    }

    public HSSFFont getSumFont(){
        if(mSumFont == null) {
            mSumFont = mWorkbook.createFont();
        }
        return mSumFont;
    }


    public HSSFFont getFont(){
        if(mFont == null) {
            mFont = mWorkbook.createFont();
        }
        return mFont;
    }

    public HSSFFont getFontDataType(){
        if(mFontDataList == null) {
            mFontDataList = mWorkbook.createFont();
        }
        return mFontDataList;
    }

    public HSSFFont getFontReport(){
        if(mFontReport == null) {
            mFontReport = mWorkbook.createFont();
        }
        return mFontReport;
    }

    public HSSFSheet createSumSheet(String sheetName){
        return mWorkbook.createSheet(sheetName);
    }

    public HSSFSheet createSumDetailSheet(String sheetName){
        return mWorkbook.createSheet(sheetName);
    }

    public HSSFSheet createSumExtensionSheet(String sheetName){
        return mWorkbook.createSheet(sheetName);
    }

    public Row createRow(Sheet sheet, int row){

        if(mRow == null) {
            mRow = sheet.createRow(row);
        }
        return mRow;
    }

    public Cell createOneCell(Row row, int iCell, String cellValue){
        Cell cell = row.createCell(iCell);
        cell.setCellValue(cellValue);
        return cell;
    }

    /**
     * 해당 row에 요청한 범위(sCol, eCol) colume을 병합
     * @param row
     * @param sCol
     * @param eCol
     */
    public void createRegionCell(Row row, int sCol, int eCol){
        CellRangeAddress region = new CellRangeAddress(row.getRowNum(),row.getRowNum(), sCol, eCol);
        mSheet.addMergedRegion(region);
    }

    /**
     * 요청한 범위(sCol, eCol) colume을 병합
     * @param sRow
     * @param eRow
     * @param sCol
     * @param eCol
     */
    public void createRegionCell(int sRow, int eRow, int sCol, int eCol){
        CellRangeAddress region = new CellRangeAddress(sRow, eRow, sCol, eCol);
        mSheet.addMergedRegion(region);
    }

    public HSSFCellStyle createCellStyle(){
        return mWorkbook.createCellStyle();
    }

    /**
     * 총괄집계표 셀스타일
     * @return
     */
    public HSSFCellStyle createAllSumCellStyle(){
        return mWorkbook.createCellStyle();
    }

    public HSSFCellStyle createCellStyleMainTitle(){
        if(mHSSFCellStyleMainTitle == null) {
            mHSSFCellStyleMainTitle = mWorkbook.createCellStyle();
        }
        return mHSSFCellStyleMainTitle;
    }

    public HSSFCellStyle createCellStyleBorderRight(){
        if(mHSSFCellStyleBorderRight == null) {
            mHSSFCellStyleBorderRight = mWorkbook.createCellStyle();
        }
        mHSSFCellStyleBorderRight.setBorderRight((short)1);
        //Cell alignment 지정하기

        mHSSFCellStyleBorderRight.setAlignment(CellStyle.ALIGN_CENTER);
        mHSSFCellStyleBorderRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return mHSSFCellStyleBorderRight;
    }

    public HSSFCellStyle createCellStyleBorderLeftRight(){
        if(mHSSFCellStyleBorderLeftRight == null) {
            mHSSFCellStyleBorderLeftRight = mWorkbook.createCellStyle();
        }
        mHSSFCellStyleBorderLeftRight.setBorderRight((short)1);
        mHSSFCellStyleBorderLeftRight.setBorderLeft((short)1);
        //Cell alignment 지정하기

        mHSSFCellStyleBorderLeftRight.setAlignment(CellStyle.ALIGN_CENTER);
        mHSSFCellStyleBorderLeftRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return mHSSFCellStyleBorderLeftRight;
    }

    public HSSFCellStyle createCellStyleBorderTopRight(){
        if(mHSSFCellStyleBorderTopRight == null) {
            mHSSFCellStyleBorderTopRight = mWorkbook.createCellStyle();
        }
        mHSSFCellStyleBorderTopRight.setBorderTop((short)1);
        mHSSFCellStyleBorderTopRight.setBorderRight((short)1);

        //Cell alignment 지정하기

        mHSSFCellStyleBorderTopRight.setAlignment(CellStyle.ALIGN_CENTER);
        mHSSFCellStyleBorderTopRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return mHSSFCellStyleBorderTopRight;
    }

    public HSSFCellStyle createCellStyleBorderAll(){
        if(mHSSFCellStyleBorderAll == null) {
            mHSSFCellStyleBorderAll = mWorkbook.createCellStyle();
        }
        mHSSFCellStyleBorderAll.setBorderTop((short)1);
        mHSSFCellStyleBorderAll.setBorderBottom((short)1);
        mHSSFCellStyleBorderAll.setBorderRight((short)1);
        mHSSFCellStyleBorderAll.setBorderLeft((short)1);

        //Cell alignment 지정하기
        mHSSFCellStyleBorderAll.setAlignment(CellStyle.ALIGN_CENTER);
        mHSSFCellStyleBorderAll.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return mHSSFCellStyleBorderAll;
    }

    /**
     * CellStyle center 정렬처리
     * @param hssfCellStyle
     */
    public void setCellCenter(HSSFCellStyle hssfCellStyle){
        hssfCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        hssfCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
    }

    public void setPaperAligment(Sheet sheet){
        sheet.setDisplayGridlines(false);
        sheet.getPrintSetup().setLandscape(false);    //가로모드 A4사이즈
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);    //출력시 A4사이즈

        sheet.setFitToPage (true);
        sheet.getPrintSetup().setFitWidth((short)1);  //가로는 1페이지에
        sheet.getPrintSetup().setFitHeight((short)0);  //세로는 자동으로

        sheet.setHorizontallyCenter(true); //출력시 가로정렬 Center로

    }

    public void setColumeWidth(Sheet sheet, SHEET_TYPE sheetType){
        if(SHEET_TYPE.SUMMARY == sheetType) {
            int columeOne = 1400;
            int columeTwo = 900;
            int columeThree = 900;
            int columeFour = 700;
            sheet.setColumnWidth(0, columeOne);  //지역분구
            sheet.setColumnWidth(1, columeOne);  //현장조사개수(개소)
            sheet.setColumnWidth(2, columeTwo);  //사다리설치 유
            sheet.setColumnWidth(3, columeTwo);  //사다리설치 무
            sheet.setColumnWidth(4, columeTwo);  //인버트설치 유
            sheet.setColumnWidth(5, columeTwo);  //인버트설치 무
            sheet.setColumnWidth(6, columeThree);  //이상항목 > 뚜껑 > 파손 > 대
            sheet.setColumnWidth(7, columeThree);  //이상항목 > 뚜껑 > 파손 > 중
            sheet.setColumnWidth(8, columeThree);  //이상항목 > 뚜껑 > 파손 > 소
            sheet.setColumnWidth(9, columeThree);  //이상항목 > 뚜껑 > 균열 > 대
            sheet.setColumnWidth(10, columeThree);  //이상항목 > 뚜껑 > 균열 > 중
            sheet.setColumnWidth(11, columeThree);  //이상항목 > 뚜껑 > 균열 > 소
            sheet.setColumnWidth(12, columeThree);  //이상항목 > 현장주변부 > 파손 > 대
            sheet.setColumnWidth(13, columeThree);  //이상항목 > 현장주변부 > 파손 > 중
            sheet.setColumnWidth(14, columeThree);  //이상항목 > 현장주변부 > 파손 > 소
            sheet.setColumnWidth(15, columeThree);  //이상항목 > 현장주변부 > 균열 > 대
            sheet.setColumnWidth(16, columeThree);  //이상항목 > 현장주변부 > 균열 > 중
            sheet.setColumnWidth(17, columeThree);  //이상항목 > 현장주변부 > 균열 > 소
            sheet.setColumnWidth(18, columeThree);  //이상항목 > 현장내부 > 파손 > 대
            sheet.setColumnWidth(19, columeThree);  //이상항목 > 현장내부 > 파손 > 중
            sheet.setColumnWidth(20, columeThree);  //이상항목 > 현장내부 > 파손 > 소
            sheet.setColumnWidth(21, columeThree);  //이상항목 > 현장내부 > 균열 > 대
            sheet.setColumnWidth(22, columeThree);  //이상항목 > 현장내부 > 균열 > 중
            sheet.setColumnWidth(23, columeThree);  //이상항목 > 현장내부 > 균열 > 소
            sheet.setColumnWidth(24, columeThree);  //이상항목 > 관로접합부 > 파손 > 대
            sheet.setColumnWidth(25, columeThree);  //이상항목 > 관로접합부 > 파손 > 중
            sheet.setColumnWidth(26, columeThree);  //이상항목 > 관로접합부 > 파손 > 소
            sheet.setColumnWidth(27, columeThree);  //이상항목 > 관로접합부 > 균열 > 대
            sheet.setColumnWidth(28, columeThree);  //이상항목 > 관로접합부 > 균열 > 중
            sheet.setColumnWidth(29, columeThree);  //이상항목 > 관로접합부 > 균열 > 소

            sheet.setColumnWidth(30, columeThree);  //침입수 > 현장주변부 >  대
            sheet.setColumnWidth(31, columeThree);  //침입수 > 현장주변부 >  중
            sheet.setColumnWidth(32, columeThree);  //침입수 > 현장주변부 >  소
            sheet.setColumnWidth(33, columeThree);  //침입수 > 현장내부 >  대
            sheet.setColumnWidth(34, columeThree);  //침입수 > 현장내부 >  중
            sheet.setColumnWidth(35, columeThree);  //침입수 > 현장내부 >  소
            sheet.setColumnWidth(36, columeThree);  //침입수 > 관로접합부 >  대
            sheet.setColumnWidth(37, columeThree);  //침입수 > 관로접합부 >  중
            sheet.setColumnWidth(38, columeThree);  //침입수 > 관로접합부 >  소
            sheet.setColumnWidth(39, columeThree);  //유입수 > 뚜껑 >  대
            sheet.setColumnWidth(40, columeThree);  //유입수 > 뚜껑 >  중
            sheet.setColumnWidth(41, columeThree);  //유입수 > 뚜껑 >  소

            sheet.setColumnWidth(42, columeFour);  //장애물 > 양호
            sheet.setColumnWidth(43, columeFour);  //장애물 > 대
            sheet.setColumnWidth(44, columeFour);  //장애물 > 중
            sheet.setColumnWidth(45, columeFour);  //장애물 > 소
            sheet.setColumnWidth(46, columeFour);  //뿌리침입 > 양호
            sheet.setColumnWidth(47, columeFour);  //뿌리침입 > 대
            sheet.setColumnWidth(48, columeFour);  //뿌리침입 > 중
            sheet.setColumnWidth(49, columeFour);  //뿌리침입 > 소
            sheet.setColumnWidth(50, columeFour);  //악취발생 > 양호
            sheet.setColumnWidth(51, columeFour);  //악취발생 > 대
            sheet.setColumnWidth(52, columeFour);  //악취발생 > 중
            sheet.setColumnWidth(53, columeFour);  //악취발생 > 소
            sheet.setColumnWidth(54, columeThree);  //프레임 > 대
            sheet.setColumnWidth(55, columeThree);  //프레임 > 중
            sheet.setColumnWidth(56, columeThree);  //프레임 > 소
            sheet.setColumnWidth(57, columeTwo);  //뚜껑밀폐 > 여 : 2개짜리
            sheet.setColumnWidth(58, columeTwo);  //뚜껑밀폐 > 부
            sheet.setColumnWidth(59, columeFour);  //매몰 > 유 : 1개짜리
            sheet.setColumnWidth(60, columeThree);  //사다리손상 > 대
            sheet.setColumnWidth(61, columeThree);  //사다리손상 > 중
            sheet.setColumnWidth(62, columeThree);  //사다리손상 > 소
            sheet.setColumnWidth(63, columeThree);  //내피생성 > 대
            sheet.setColumnWidth(64, columeThree);  //내피생성 > 중
            sheet.setColumnWidth(65, columeThree);  //내피생성 > 소
            sheet.setColumnWidth(66, columeThree);  //폐유부착 > 대
            sheet.setColumnWidth(67, columeThree);  //폐유부착 > 중
            sheet.setColumnWidth(68, columeThree);  //폐유부착 > 소
            sheet.setColumnWidth(69, columeThree);  //블록이음부단차 > 대
            sheet.setColumnWidth(70, columeThree);  //블록이음부단차 > 중
            sheet.setColumnWidth(71, columeThree);  //블록이음부단차 > 소
            sheet.setColumnWidth(72, columeThree);  //블록이음부손상 > 대
            sheet.setColumnWidth(73, columeThree);  //블록이음부손상 > 중
            sheet.setColumnWidth(74, columeThree);  //블록이음부손상 > 소
            sheet.setColumnWidth(75, columeThree);  //블록이음부이탈 > 대
            sheet.setColumnWidth(76, columeThree);  //블록이음부이탈 > 중
            sheet.setColumnWidth(77, columeThree);  //블록이음부이탈 > 소
            sheet.setColumnWidth(78, columeThree);  //표면단차 > 대
            sheet.setColumnWidth(79, columeThree);  //표면단차 > 중
            sheet.setColumnWidth(80, columeThree);  //표면단차 > 소
        }else if(SHEET_TYPE.SUMMARY_DETAIL == sheetType) {
            int columeOne = 1400;
            int columeTwo = 1200;
            int columeText = 2400;
            int columeMhNum = 1600;
            int columeBlock = 1800;

            sheet.setColumnWidth(0, columeTwo);  //야장쪽수
            sheet.setColumnWidth(1, columeOne);  //처리분구
            sheet.setColumnWidth(2, columeMhNum);  //현장번호
            sheet.setColumnWidth(3, columeOne);  //배제방식
            sheet.setColumnWidth(4, columeOne);  //사다리설치
            sheet.setColumnWidth(5, columeOne);  //인버트설치
            sheet.setColumnWidth(6, columeTwo);  //이상항목 >뚜껑 > 파손 > 대
            sheet.setColumnWidth(7, columeTwo);  //이상항목 > 뚜껑 > 균열 > 대
            sheet.setColumnWidth(8, columeTwo);  //이상항목 > 현장주변부 > 파손 > 대
            sheet.setColumnWidth(9, columeTwo);  //이상항목 > 현장주변부 > 균열 > 대
            sheet.setColumnWidth(10, columeTwo);  //이상항목 > 현장내부 > 파손 > 대
            sheet.setColumnWidth(11, columeTwo);  //이상항목 > 현장내부 > 균열 > 대
            sheet.setColumnWidth(12, columeTwo);  //이상항목 > 관로접합부 > 파손 > 대
            sheet.setColumnWidth(13, columeTwo);  //이상항목 > 관로접합부 > 균열 > 대
            sheet.setColumnWidth(14, columeOne);  //침입수 > 현장주변부 >  대
            sheet.setColumnWidth(15, columeOne);  //침입수 > 현장내부 >  대
            sheet.setColumnWidth(16, columeOne);  //침입수 > 관로접합부 >  대
            sheet.setColumnWidth(17, columeOne);  //유입수 > 뚜껑 >  대
            sheet.setColumnWidth(18, columeOne);  //장애물 > 양호
            sheet.setColumnWidth(19, columeOne);  //뿌리침입 > 양호
            sheet.setColumnWidth(20, columeOne);  //악취발생 > 양호
            sheet.setColumnWidth(21, columeOne);  //뚜껑밀폐 > 여 : 2개짜리
            sheet.setColumnWidth(22, columeTwo);  //매몰 > 유 : 1개짜리
            sheet.setColumnWidth(23, columeOne);  //사다리손상 > 대
            sheet.setColumnWidth(24, columeOne);  //내피생성 > 대
            sheet.setColumnWidth(25, columeOne);  //폐유부착 > 대
            sheet.setColumnWidth(26, columeBlock);  //블록이음부단차 > 대
            sheet.setColumnWidth(27, columeBlock);  //블록이음부손상 > 대
            sheet.setColumnWidth(28, columeBlock);  //블록이음부이탈 > 대
            sheet.setColumnWidth(29, columeOne);  //표면단차 > 대
            sheet.setColumnWidth(30, columeText);  //특이사항
        }else if(SHEET_TYPE.SUMMARY_EXTENSION == sheetType) {
            int columeNo = 1600;
            int columeTwo = 2000;
            int columeMhNum = 2400;

            sheet.setColumnWidth(0, columeNo);  //연번
            sheet.setColumnWidth(1, columeMhNum);  //조사일
            sheet.setColumnWidth(2, columeTwo);    //처리분구
            sheet.setColumnWidth(3, columeMhNum);  //현장번호 시점
            sheet.setColumnWidth(4, columeMhNum);  //현장번호 종점
            sheet.setColumnWidth(5, columeTwo);  //배제방식
            sheet.setColumnWidth(6, columeTwo);  //관종
            sheet.setColumnWidth(7, columeTwo);  //관경
            sheet.setColumnWidth(8, columeTwo);  //연장
            sheet.setColumnWidth(9, columeTwo);  //비고
        }else {
            sheet.setColumnWidth(0, 1000);  //No.
            sheet.setColumnWidth(1, 1000); //처리구역
            sheet.setColumnWidth(2, 1000);
            sheet.setColumnWidth(3, 1400); //배제방식
            sheet.setColumnWidth(4, 1000);
            sheet.setColumnWidth(5, 800);  //현장번호
            sheet.setColumnWidth(6, 800);
            sheet.setColumnWidth(7, 1200);
            sheet.setColumnWidth(8, 1600);  //규격
            sheet.setColumnWidth(9, 1200);
            sheet.setColumnWidth(10, 1200); //현장깊이
            sheet.setColumnWidth(11, 1200);
            sheet.setColumnWidth(12, 1000); //GPS위도
            sheet.setColumnWidth(13, 1000);
            sheet.setColumnWidth(14, 1000);
            sheet.setColumnWidth(15, 1000); //GPS경도
            sheet.setColumnWidth(16, 1000);
            sheet.setColumnWidth(17, 1000);
            sheet.setColumnWidth(18, 1200); //조사자
            sheet.setColumnWidth(19, 1200);
        }
    }

    public enum PHOTO_TYPE{
        AROUND,
        OUTER,
        INNER,
        ETC,
        CAD,
    }

    HashMap<String, Uri> photoMap = new HashMap<String, Uri>();
    HashMap<String, File> photoFileMap = new HashMap<String, File>();

    /**
     * FieldDetailForm에서 카메라, 앨범 > 저장한 각 이미지 저장
     */
    public void setPhotoUri(PHOTO_TYPE photoType, Uri photo){
        photoMap.put(photoType.name(), photo);
    }

    public void setPhotoFileMap(PHOTO_TYPE photoType, File photoFile){
        GomsLog.d(LOG_TAG, "setPhotoFileMap()");
        GomsLog.d(LOG_TAG, "setPhotoFileMap() photoType : " + photoType.name());
        GomsLog.d(LOG_TAG, "setPhotoFileMap() photoFile.getName() : " + photoFile.getName());
        photoFileMap.put(photoType.name(), photoFile);
    }
    public HashMap<String, File>  getPhotoFileMap(){
        return photoFileMap;
    }

    public void deletePhotoFileMapItem(String key){
        photoFileMap.remove(key);
    }
    public void clearPhotoFileMap(){
        photoFileMap.clear();
    }

    private void setServerPhoto(HSSFSheet sheet, FieldDetailBeanS fieldDetailBeanS) throws ExecutionException, InterruptedException {

        int pictureAround = -1;
        int pictureOuter = -1;
        int pictureInner = -1;
        int pictureEtc = -1;
        int pictureCad = -1;

        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_around())) {
            byte[] bytesAround = Glide.with(mActivity)
                    .as(byte[].class)
                    .load(fieldDetailBeanS.getRes_mh_photo_around())
                    .submit()
                    .get();

            pictureAround = mWorkbook.addPicture(bytesAround, HSSFWorkbook.PICTURE_TYPE_JPEG);
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_outer())) {
            byte[] bytesOuter = Glide.with(mActivity)
                    .as(byte[].class)
                    .load(fieldDetailBeanS.getRes_mh_photo_outer())
                    .submit()
                    .get();
            pictureOuter = mWorkbook.addPicture(bytesOuter, HSSFWorkbook.PICTURE_TYPE_JPEG);
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_inner())) {
            byte[] bytesInner = Glide.with(mActivity)
                    .as(byte[].class)
                    .load(fieldDetailBeanS.getRes_mh_photo_inner())
                    .submit()
                    .get();
            pictureInner = mWorkbook.addPicture(bytesInner, HSSFWorkbook.PICTURE_TYPE_JPEG);
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_etc())) {
            byte[] bytesEtc = Glide.with(mActivity)
                    .as(byte[].class)
                    .load(fieldDetailBeanS.getRes_mh_photo_etc())
                    .submit()
                    .get();
            pictureEtc = mWorkbook.addPicture(bytesEtc, HSSFWorkbook.PICTURE_TYPE_JPEG);
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_cad())) {
            byte[] bytesCad = Glide.with(mActivity)
                    .as(byte[].class)
                    .load(fieldDetailBeanS.getRes_mh_cad())
                    .submit()
                    .get();
            pictureCad = mWorkbook.addPicture(bytesCad, HSSFWorkbook.PICTURE_TYPE_JPEG);
        }


        CreationHelper creationHelper = mWorkbook.getCreationHelper();
        HSSFPatriarch hssfPatriarch = sheet.createDrawingPatriarch();

        ClientAnchor clientAnchorAround = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorOuter = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorInner = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorEtc = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorCad = creationHelper.createClientAnchor();

        clientAnchorAround.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorOuter.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorInner.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorEtc.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorCad.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);

        if ("Y".equals(MyApplication.getInstance().prefs().get(ManHolePrefs.MH_BLOCK_IMPORT_YN, "N"))) {
            clientAnchorAround.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.firstRow);
            clientAnchorAround.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.lastRow + 1);
            clientAnchorAround.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.firstCol);
            clientAnchorAround.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.lastCol + 1);

            clientAnchorOuter.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.firstRow);
            clientAnchorOuter.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.lastRow + 1);
            clientAnchorOuter.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.firstCol);
            clientAnchorOuter.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.lastCol + 1);

            clientAnchorInner.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.firstRow);
            clientAnchorInner.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.lastRow + 1);
            clientAnchorInner.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.firstCol);
            clientAnchorInner.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.lastCol + 1);

            clientAnchorEtc.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.firstRow);
            clientAnchorEtc.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.lastRow + 1);
            clientAnchorEtc.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.firstCol);
            clientAnchorEtc.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.lastCol + 1);
        }else{
            clientAnchorAround.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG_ELSE.firstRow);
            clientAnchorAround.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG_ELSE.lastRow + 1);
            clientAnchorAround.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG_ELSE.firstCol);
            clientAnchorAround.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG_ELSE.lastCol + 1);

            clientAnchorOuter.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG_ELSE.firstRow);
            clientAnchorOuter.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG_ELSE.lastRow + 1);
            clientAnchorOuter.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG_ELSE.firstCol);
            clientAnchorOuter.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG_ELSE.lastCol + 1);

            clientAnchorInner.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG_ELSE.firstRow);
            clientAnchorInner.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG_ELSE.lastRow + 1);
            clientAnchorInner.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG_ELSE.firstCol);
            clientAnchorInner.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG_ELSE.lastCol + 1);

            clientAnchorEtc.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG_ELSE.firstRow);
            clientAnchorEtc.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG_ELSE.lastRow + 1);
            clientAnchorEtc.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG_ELSE.firstCol);
            clientAnchorEtc.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG_ELSE.lastCol + 1);
        }

        clientAnchorCad.setRow1(CELL_STYLE.VALUE_TITLE_MH_CAD.firstRow);
        clientAnchorCad.setRow2(CELL_STYLE.VALUE_TITLE_MH_CAD.lastRow + 1);
        clientAnchorCad.setCol1(CELL_STYLE.VALUE_TITLE_MH_CAD.firstCol);
        clientAnchorCad.setCol2(CELL_STYLE.VALUE_TITLE_MH_CAD.lastCol + 1);

        //Creates a picture
        if (pictureAround > 0) {
            hssfPatriarch.createPicture(clientAnchorAround, pictureAround);
        }
        if (pictureOuter > 0) {
            hssfPatriarch.createPicture(clientAnchorOuter, pictureOuter);
        }
        if (pictureInner > 0) {
            hssfPatriarch.createPicture(clientAnchorInner, pictureInner);
        }
        if (pictureEtc > 0){
            hssfPatriarch.createPicture(clientAnchorEtc, pictureEtc);
        }
        if (pictureCad > 0) {
            hssfPatriarch.createPicture(clientAnchorCad, pictureCad);
        }
    }

    /**
     * 현장조사야장 > 각 사진 넣기 처리
     * setManholeExcel에서 호출합니다.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void setPhoto() throws ExecutionException, InterruptedException {

        Uri photoAround = null;
        Uri photoOuter = null;
        Uri photoInner = null;
        Uri photoEtc = null;
        Uri cad = null;

        int pictureAround = -1;
        int pictureOuter = -1;
        int pictureInner = -1;
        int pictureEtc = -1;
        int pictureCad = -1;

        byte[] bytes;

        for (Map.Entry<String, Uri> entry : photoMap.entrySet()) {
            String key = entry.getKey();
            Uri uri = entry.getValue();
            if(PHOTO_TYPE.AROUND.name().equals(key)){
                photoAround = uri;
                bytes = ImageUtil.convertUriToByteArray(mActivity, photoAround);
                pictureAround = mWorkbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG);
            }else if(PHOTO_TYPE.OUTER.name().equals(key)){
                photoOuter = uri;
                bytes = ImageUtil.convertUriToByteArray(mActivity, photoOuter);
                pictureOuter = mWorkbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG);
            }else if(PHOTO_TYPE.INNER.name().equals(key)){
                photoInner = uri;
                bytes = ImageUtil.convertUriToByteArray(mActivity, photoInner);
                pictureInner = mWorkbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG);
            }else if(PHOTO_TYPE.ETC.name().equals(key)){
                photoEtc = uri;
                bytes = ImageUtil.convertUriToByteArray(mActivity, photoEtc);
                pictureEtc = mWorkbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG);
            }else if(PHOTO_TYPE.CAD.name().equals(key)){
                cad = uri;
                bytes = ImageUtil.convertUriToByteArray(mActivity, cad);
                pictureCad = mWorkbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG);
            }
        }

        CreationHelper creationHelper = mWorkbook.getCreationHelper();
        HSSFPatriarch hssfPatriarch = mSheet.createDrawingPatriarch();

        ClientAnchor clientAnchorAround = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorOuter = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorInner = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorEtc = creationHelper.createClientAnchor();
        ClientAnchor clientAnchorCad = creationHelper.createClientAnchor();

        clientAnchorAround.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorOuter.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorInner.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorEtc.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
        clientAnchorCad.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);

        clientAnchorAround.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.firstRow);
        clientAnchorAround.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.lastRow + 1);
        clientAnchorAround.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.firstCol);
        clientAnchorAround.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.lastCol + 1);

        clientAnchorOuter.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.firstRow);
        clientAnchorOuter.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.lastRow + 1);
        clientAnchorOuter.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.firstCol);
        clientAnchorOuter.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG.lastCol + 1);

        clientAnchorInner.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.firstRow);
        clientAnchorInner.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.lastRow + 1);
        clientAnchorInner.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.firstCol);
        clientAnchorInner.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.lastCol + 1);

        clientAnchorEtc.setRow1(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.firstRow);
        clientAnchorEtc.setRow2(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.lastRow + 1);
        clientAnchorEtc.setCol1(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.firstCol);
        clientAnchorEtc.setCol2(CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG.lastCol + 1);

        clientAnchorCad.setRow1(CELL_STYLE.VALUE_TITLE_MH_CAD.firstRow);
        clientAnchorCad.setRow2(CELL_STYLE.VALUE_TITLE_MH_CAD.lastRow + 1);
        clientAnchorCad.setCol1(CELL_STYLE.VALUE_TITLE_MH_CAD.firstCol);
        clientAnchorCad.setCol2(CELL_STYLE.VALUE_TITLE_MH_CAD.lastCol + 1);

        //Creates a picture
        if (pictureAround > 0) {
            HSSFPicture hssfPictureAround = hssfPatriarch.createPicture(clientAnchorAround, pictureAround);
        }
        if (pictureOuter > 0) {
            HSSFPicture hssfPictureOuter = hssfPatriarch.createPicture(clientAnchorOuter, pictureOuter);
        }
        if (pictureInner > 0) {
            HSSFPicture hssfPictureInner = hssfPatriarch.createPicture(clientAnchorInner, pictureInner);
        }
        if (pictureEtc > 0){
            HSSFPicture hssfPictureEtc = hssfPatriarch.createPicture(clientAnchorEtc, pictureEtc);
        }
        if (pictureCad > 0) {
            HSSFPicture hssfPictureCad = hssfPatriarch.createPicture(clientAnchorCad, pictureCad);
        }
    }

    /**
     * 총괄집계표 스타일
     * @param row
     * @param cellStyle
     * @param text
     */
    public void setSumStyleBg(HSSFSheet sheet, Row row, SUM_CELL_STYLE cellStyle,@Nullable String text, HSSFCellStyle hssfCellStyle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        CreationHelper creationHelper = mWorkbook.getCreationHelper();

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        cell.setCellValue(cellValue);

        //"현장 조사 개수(개소)"에 대한 행간처리
        if(SUM_CELL_STYLE.COUNT.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_CELL_STYLE.COUNT.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);
            }

        }
    }
    public void setSumStyle(HSSFSheet sheet, Row row, SUM_CELL_STYLE cellStyle,@Nullable String text, HSSFCellStyle hssfCellStyle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        cell.setCellValue(cellValue);
        cell.setCellStyle(hssfCellStyle);

    }

    /**
     * 상세집계표 스타일
     * @param row
     * @param cellStyle
     * @param text
     */
    public void setSumDetailStyleBg(HSSFSheet sheet, CellStyle callStyleBg, Row row, SUM_DETAIL_CELL_STYLE cellStyle,@Nullable String text){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        CreationHelper creationHelper = mWorkbook.getCreationHelper();

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        //"야장쪽수"에 대한 행간처리
        if(SUM_DETAIL_CELL_STYLE.NO.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.NO.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.LOCAL.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.LOCAL.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.LADDER_YN.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.LADDER_YN.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.INVERT_YN.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.INVERT_YN.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.BLOCK_GAP.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.BLOCK_GAP.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            callStyleBg.setWrapText(true);
        }else{
            cell.setCellValue(cellValue);
        }

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(callStyleBg);
            }
        }
    }

    public void setSumDetailStyle(HSSFSheet sheet, Font font, Row row, SUM_DETAIL_CELL_STYLE cellStyle,@Nullable String text){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //CellStyle 객체생성
        HSSFCellStyle hssfCellStyle = createCellStyle();
        //Cell alignment 지정하기
        hssfCellStyle.setAlignment(cellStyle.hAlignCenter);
        hssfCellStyle.setVerticalAlignment(cellStyle.vAlignCenter);

        CreationHelper creationHelper = mWorkbook.getCreationHelper();

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        //"야장쪽수"에 대한 행간처리
        if(SUM_DETAIL_CELL_STYLE.NO.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.NO.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.LOCAL.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.LOCAL.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.LADDER_YN.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.LADDER_YN.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.INVERT_YN.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.INVERT_YN.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.BLOCK_GAP.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.BLOCK_GAP.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else{
            cell.setCellValue(cellValue);
        }

        //Font 객체생성
        font.setBold(cellStyle.bold);
        font.setFontHeight(cellStyle.fontHeight);
        font.setUnderline(cellStyle.underline);
        //CellStyle에 지정한 폰트 Set
        hssfCellStyle.setFont(font);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        hssfCellStyle.setBorderTop((short)1);
        hssfCellStyle.setBorderBottom((short)1);
        hssfCellStyle.setBorderLeft((short)1);
        hssfCellStyle.setBorderRight((short)1);

        if(CELL_TYPE_BG.Y.name().equals(cellStyle.cellTypeBg.name())) {
            hssfCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            hssfCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            hssfCellStyle.setFillPattern((short) 1);
        }

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);
            }

        }

        //HSSFCellStyle hssfCellStyleRight = createCellStyleBorderRight();
        HSSFCellStyle hssfCellStyleRight = mWorkbook.createCellStyle();
        hssfCellStyleRight.setBorderRight((short)1);
        //Cell alignment 지정하기
        hssfCellStyleRight.setAlignment(CellStyle.ALIGN_CENTER);
        hssfCellStyleRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);


        Row rowDiv3 = sheet.getRow(3);
        if(rowDiv3 == null){
            rowDiv3= sheet.createRow(3);
        }

        for(int i = 14; i < 18; i++){
            Cell iCell;
            try {
                iCell = rowDiv3.getCell(i);
                if (iCell == null) {
                    iCell = rowDiv3.createCell(i);
                }
            }catch (NullPointerException e){
                iCell = rowDiv3.createCell(i);
            }
            iCell.setCellStyle(hssfCellStyleRight);
        }

        HashMap<Integer, SUM_DETAIL_CELL_STYLE> sumCellStyleHashMap = new HashMap<Integer, SUM_DETAIL_CELL_STYLE>();
        sumCellStyleHashMap.put(1, SUM_DETAIL_CELL_STYLE.NO);
        sumCellStyleHashMap.put(2, SUM_DETAIL_CELL_STYLE.LOCAL);
        sumCellStyleHashMap.put(3, SUM_DETAIL_CELL_STYLE.MH_NUM);
        sumCellStyleHashMap.put(4, SUM_DETAIL_CELL_STYLE.MH_DRAINAGE);
        sumCellStyleHashMap.put(5, SUM_DETAIL_CELL_STYLE.LADDER_YN);
        sumCellStyleHashMap.put(6, SUM_DETAIL_CELL_STYLE.INVERT_YN);
        sumCellStyleHashMap.put(7, SUM_DETAIL_CELL_STYLE.OBSTACLE);
        sumCellStyleHashMap.put(8, SUM_DETAIL_CELL_STYLE.ROOT_INTRUSION);
        sumCellStyleHashMap.put(9, SUM_DETAIL_CELL_STYLE.ODOR);
        sumCellStyleHashMap.put(10, SUM_DETAIL_CELL_STYLE.LID_SEALING);
        sumCellStyleHashMap.put(11, SUM_DETAIL_CELL_STYLE.BURIED);
        sumCellStyleHashMap.put(12, SUM_DETAIL_CELL_STYLE.LADDER_DAMAGE);
        sumCellStyleHashMap.put(13, SUM_DETAIL_CELL_STYLE.ENDOTHELIUM);
        sumCellStyleHashMap.put(14, SUM_DETAIL_CELL_STYLE.WASTEOIL);
        sumCellStyleHashMap.put(15, SUM_DETAIL_CELL_STYLE.BLOCK_GAP);
        sumCellStyleHashMap.put(16, SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE);
        sumCellStyleHashMap.put(17, SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE);
        sumCellStyleHashMap.put(18, SUM_DETAIL_CELL_STYLE.SURFACE_GAP);
        sumCellStyleHashMap.put(19, SUM_DETAIL_CELL_STYLE.MH_REMARK);

        //row 2,3에 대해서 우측 라인 긋기
        for(int k = 2; k < 4; k++) {
            Row iRow = sheet.getRow(k);
            if(iRow == null){
                iRow = sheet.createRow(k);
            }

            for (Map.Entry<Integer, SUM_DETAIL_CELL_STYLE> entry : sumCellStyleHashMap.entrySet()) {
                SUM_DETAIL_CELL_STYLE sumCellStyle = entry.getValue();

                for (int i = sumCellStyle.firstCol; i <= sumCellStyle.lastCol; i++) {
                    Cell iCell;
                    try {
                        iCell = iRow.getCell(i);
                        if (iCell == null) {
                            iCell = iRow.createCell(i);
                        }
                    } catch (NullPointerException e) {
                        iCell = iRow.createCell(i);
                    }
                    iCell.setCellStyle(hssfCellStyleRight);
                }
            }
        }
    }


    /**
     * 상세집계표 마지막 Row가 합계를 처리함.
     * @param sheet
     * @param row
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @param text
     */
    public void setSumDetailSummaryStyleBg(HSSFSheet sheet, Row row, int firstRow, int lastRow, int firstCol, int lastCol, @Nullable String text, CellStyle hssfCellStyleBg){

        short height = (short) 300;

        Cell cell = row.createCell(firstCol);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(height);

        //해당 셀에 타이틀 지정하기
        String cellValue = text;
        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }
                borderCell.setCellStyle(hssfCellStyleBg);
            }
        }



    }
    public void setSumDetailSummaryStyle(HSSFSheet sheet, Row row, int firstRow, int lastRow, int firstCol, int lastCol, @Nullable String text, CellStyle hssfCellStyle){

        short height = (short) 300;

        Cell cell = row.createCell(firstCol);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(height);

        //해당 셀에 타이틀 지정하기
        String cellValue = text;
        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);
            }
        }



    }


    /**
     * 연장집계표 스타일
     * @param row
     * @param cellStyle
     * @param text
     */
    public void setSumExtensionStyleBg(HSSFSheet sumExtensionSheet, Row row, SUM_EXTENSION_CELL_STYLE cellStyle,@Nullable String text, HSSFCellStyle hssfCellStyle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        CreationHelper creationHelper = mWorkbook.getCreationHelper();

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        //"연변"에 대한 행간처리
        if(SUM_EXTENSION_CELL_STYLE.NO.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_EXTENSION_CELL_STYLE.NO.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2 * sumExtensionSheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else{
            cell.setCellValue(cellValue);
        }

        //row, col 병합 처리하기
        int iRegion = sumExtensionSheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sumExtensionSheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sumExtensionSheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sumExtensionSheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);
            }

        }
    }
    public void setSumExtensionStyle(HSSFSheet sumExtensionSheet, Row row, SUM_EXTENSION_CELL_STYLE cellStyle,@Nullable String text){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //CellStyle 객체생성
        HSSFCellStyle hssfCellStyle = createCellStyle();
        //Cell alignment 지정하기
        hssfCellStyle.setAlignment(cellStyle.hAlignCenter);
        hssfCellStyle.setVerticalAlignment(cellStyle.vAlignCenter);

        CreationHelper creationHelper = mWorkbook.getCreationHelper();

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        //"연변"에 대한 행간처리
        if(SUM_EXTENSION_CELL_STYLE.NO.equals(cellStyle)) {
            RichTextString richText = creationHelper.createRichTextString(SUM_EXTENSION_CELL_STYLE.NO.title);
            cell.setCellValue(richText);
            row.setHeightInPoints((2 * sumExtensionSheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else{
            cell.setCellValue(cellValue);
        }

        //Font 객체생성
        Font font = getFont();
        font.setBold(cellStyle.bold);
        font.setFontHeight(cellStyle.fontHeight);
        font.setUnderline(cellStyle.underline);
        //CellStyle에 지정한 폰트 Set
        hssfCellStyle.setFont(font);

        //row, col 병합 처리하기
        int iRegion = sumExtensionSheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        hssfCellStyle.setBorderTop((short)1);
        hssfCellStyle.setBorderBottom((short)1);
        hssfCellStyle.setBorderLeft((short)1);
        hssfCellStyle.setBorderRight((short)1);

        //CellStyle 객체생성
        HSSFCellStyle hssfCellStyleMainTitle = createCellStyleMainTitle();
        hssfCellStyleMainTitle.setBorderTop((short)1);
        hssfCellStyleMainTitle.setBorderLeft((short)1);
        hssfCellStyleMainTitle.setBorderRight((short)1);
        hssfCellStyleMainTitle.setAlignment(cellStyle.hAlignCenter);
        hssfCellStyleMainTitle.setVerticalAlignment(cellStyle.vAlignCenter);
        hssfCellStyleMainTitle.setFont(font);

        if(CELL_TYPE_BG.Y.name().equals(cellStyle.cellTypeBg.name())) {
            hssfCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            hssfCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            hssfCellStyle.setFillPattern((short) 1);
        }

        CellRangeAddress mergedRegion = sumExtensionSheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sumExtensionSheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sumExtensionSheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);
            }

        }
    }

    /**
     *현장조사야장 제목 스타일
     */
    private void setMainTitleStyle(HSSFSheet sheet, Row row , CELL_STYLE cellStyle, @Nullable String text, HSSFCellStyle cellStyleMainTitle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }
        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(cellStyleMainTitle);
            }

        }
    }

    /**
     * 현장조사 총괄집계표 제목 스타일
     * @param sheet
     * @param row
     * @param cellStyle
     * @param text
     * @param cellStyleMainTitle
     */
    private void setSumMainTitleStyle(HSSFSheet sheet, Row row , SUM_CELL_STYLE cellStyle, @Nullable String text, HSSFCellStyle cellStyleMainTitle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }
        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(cellStyleMainTitle);
            }

        }
    }

    /**
     * 현장조사 집계표
     * @param sheet
     * @param row
     * @param cellStyle
     * @param text
     * @param cellStyleMainTitle
     */
    private void setSumDetailMainTitleStyle(HSSFSheet sheet, Row row , SUM_DETAIL_CELL_STYLE cellStyle, @Nullable String text, HSSFCellStyle cellStyleMainTitle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }
        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(cellStyleMainTitle);
            }

        }
    }
    private void setExtensionMainTitleStyle(HSSFSheet sheet, Row row , SUM_EXTENSION_CELL_STYLE cellStyle, @Nullable String text, HSSFCellStyle cellStyleMainTitle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }
        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(cellStyleMainTitle);
            }

        }
    }

    /**
     * 현장 조사 야장
     * MainTitle row0, CELL_STYLE_MAIN
     * @param row
     * @param cellStyle
     */
    public void setStyleBg(HSSFSheet sheet, Row row, CELL_STYLE cellStyle,@Nullable String text, HSSFCellStyle hssfCellStyle, CreationHelper creationHelper){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        if(CELL_STYLE.HEAD_TITLE_MH_LID_SEALING.title.equals(cellStyle.title)) {
            RichTextString richText = creationHelper.createRichTextString("뚜껑밀페\n(개폐불가)");
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(CELL_STYLE.HEAD_TITLE_MH_OUTER_WATER.title.equals(cellStyle.title)) {
            RichTextString richText = creationHelper.createRichTextString("침입수등급\n(대중소)");
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else if(CELL_STYLE.HEAD_TITLE_MH_LADDER_DAMAGE_LMS.title.equals(cellStyle.title)
                || (CELL_STYLE.HEAD_TITLE_MH_BLOCK_GAP_LMS.equals(cellStyle.title))){
            RichTextString richText = creationHelper.createRichTextString("대\n중\n소");
            cell.setCellValue(richText);
            row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));
            hssfCellStyle.setWrapText(true);
        }else{
            cell.setCellValue(cellValue);
        }

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }
                borderCell.setCellStyle(hssfCellStyle);
            }

        }
    }
    public void setStyle(HSSFSheet sheet, Row row, CELL_STYLE cellStyle,@Nullable String text, HSSFCellStyle hssfCellStyle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);

            }

        }
    }

    public void setStyleDiv(HSSFSheet sheet, Row row, CELL_STYLE cellStyle,@Nullable String text){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }
            }

        }
    }

    public void setStyleRemark(HSSFSheet sheet, Row row, CELL_STYLE cellStyle,@Nullable String text, HSSFCellStyle hssfCellStyle){

        Cell cell = row.createCell(cellStyle.col);

        //해당 셀의 Row 높이 지정하기
        row.setHeight(cellStyle.rowHeight);

        //해당 셀에 타이틀 지정하기
        String cellValue = cellStyle.title;
        if(!StringUtil.isEmpty(text)){
            cellValue = text;
        }

        cell.setCellValue(cellValue);

        //row, col 병합 처리하기
        int iRegion = sheet.addMergedRegion(new CellRangeAddress(cellStyle.firstRow, cellStyle.lastRow, cellStyle.firstCol, cellStyle.lastCol));

        CellRangeAddress mergedRegion = sheet.getMergedRegion(iRegion);
        for (int iRow = mergedRegion.getFirstRow(); iRow <= mergedRegion.getLastRow(); iRow++) {

            Row currentRow = sheet.getRow(iRow);
            if (currentRow == null) {
                currentRow = sheet.createRow(iRow);
            }

            for (int iCol = mergedRegion.getFirstColumn(); iCol <= mergedRegion.getLastColumn(); iCol++) {
                Cell borderCell;
                try {
                    borderCell = currentRow.getCell(iCol);
                    if (borderCell == null) {
                        borderCell = currentRow.createCell(iCol);
                    }
                }catch(NullPointerException e){
                    borderCell = currentRow.getCell(iCol);
                }

                borderCell.setCellStyle(hssfCellStyle);

            }

        }
    }


    /**
     * 엑셀 생성 시작!! > 현장조사 총괄집계표
     */
    public void createManholeSumExcel(FieldBasicBeanS fieldBasicBeanS, FieldDetailListSumBeanS fieldDetailListSumBeanS, ArrayList<FieldDetailBeanS> mFieldDetailList){
        GomsLog.d(LOG_TAG, "createManholeSumExcel()");

        HSSFSheet sumSheet = createSumSheet("현장조사 총괄집계표");

        //row 5, col 80까지 생성
        /*
        for(int rowIndex = 0; rowIndex < SUM_CELL_STYLE.MAIN_TITLE.row; rowIndex++){
            Row row = createRow(mSumSheet, rowIndex);
            for (int colIndex = 0; colIndex < SUM_CELL_STYLE.VALUE_LOCAL.col; colIndex++) {
                row.createCell(colIndex);
            }
        }
         */
        //총괄집계표 폰트 생성
        Font font = getSumFont();

        //첫번째 행 >> 현장조사 총괄 집계표

        Font fontMainTitle = mWorkbook.createFont();
        fontMainTitle.setBold(true);
        fontMainTitle.setFontHeight(TITLE_SIZE.SUM_TITLE.size);
        fontMainTitle.setUnderline(U_NONE);

        //CellStyle 객체생성 - 메인타이틀
        HSSFCellStyle hssfCellStyleMainTitle = createCellStyle();
        //Cell alignment 지정하기
        short hAlignCenter = CellStyle.ALIGN_CENTER;
        short vAlignCenter = CellStyle.VERTICAL_CENTER;
        hssfCellStyleMainTitle.setAlignment(hAlignCenter);
        hssfCellStyleMainTitle.setVerticalAlignment(vAlignCenter);
        hssfCellStyleMainTitle.setFont(fontMainTitle);

        Row rowSumMainTitle = sumSheet.createRow(SUM_CELL_STYLE.MAIN_TITLE.row);
        setSumMainTitleStyle(sumSheet, rowSumMainTitle, SUM_CELL_STYLE.MAIN_TITLE, "현장조사 총괄 집계표", hssfCellStyleMainTitle);

        Font fontReportBg = mWorkbook.createFont();
        fontReportBg.setBold(false);
        fontReportBg.setFontHeight(TITLE_SIZE.TITLE.size);
        fontReportBg.setUnderline(U_NONE);

        Font fontReportValue = mWorkbook.createFont();
        fontReportValue.setBold(false);
        fontReportValue.setFontHeight(TITLE_SIZE.TITLE.size);
        fontReportValue.setUnderline(U_NONE);

        //CellStyle 객체생성 - 배경
        HSSFCellStyle hssfCellStyleBg = createCellStyle();
        hssfCellStyleBg.setAlignment(hAlignCenter);
        hssfCellStyleBg.setVerticalAlignment(vAlignCenter);
        hssfCellStyleBg.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillPattern((short) 1);
        hssfCellStyleBg.setBorderTop((short)1);
        hssfCellStyleBg.setBorderBottom((short)1);
        hssfCellStyleBg.setBorderLeft((short)1);
        hssfCellStyleBg.setBorderRight((short)1);
        hssfCellStyleBg.setFont(fontReportBg);

        //CellStyle 객체생성 - 값
        HSSFCellStyle hssfCellStyleValue = createCellStyle();
        hssfCellStyleValue.setAlignment(hAlignCenter);
        hssfCellStyleValue.setVerticalAlignment(vAlignCenter);
        hssfCellStyleValue.setBorderTop((short)1);
        hssfCellStyleValue.setBorderBottom((short)1);
        hssfCellStyleValue.setBorderLeft((short)1);
        hssfCellStyleValue.setBorderRight((short)1);
        hssfCellStyleValue.setFont(fontReportValue);


        //두번째 행
        Row rowSumTitle = sumSheet.createRow(SUM_CELL_STYLE.LOCAL.row);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.LOCAL, "처리\n분구", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.COUNT, "현장\n조사\n개수\n(개소)", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.LADDER_YN, "사다리설치", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.INVERT_YN, "인버트설치", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.ABNORMAL_ITEM, "이상항목", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.WATER_INVASION, "침입수", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.WATER_INNER, "유입수", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.OBSTACLE, "장애물", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.ROOT_INTRUSION, "뿌리침입", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.ODOR, "악취발생", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.LID_SEALING, "뚜껑밀폐", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.BURIED, "매몰", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.LADDER_DAMAGE, "사다리손상", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.ENDOTHELIUM, "내피생성", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.WASTEOIL, "폐유부착", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.BLOCK_GAP, "블록이음부단차", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.BLOCK_DAMAGE, "블록이음부손상", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.BLOCK_LEAVE, "블록이음부이탈", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumTitle, SUM_CELL_STYLE.SURFACE_GAP, "표면단차", hssfCellStyleBg);

        //세번째 행
        Row rowSumSubTitle = sumSheet.createRow(SUM_CELL_STYLE.MH_LID.row);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_LID, "뚜껑", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_OUTER, "현장주변부", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_INNER, "현장내부", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_PIPE, "관로접합부", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_WATER_INVASION_OUTER, "현장주변부", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_WATER_INVASION_INNER, "현장내부", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_WATER_INVASION_PIPE, "관로접합부", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumSubTitle, SUM_CELL_STYLE.MH_WATER_INNER_LID, "뚜껑", hssfCellStyleBg);

        //네번째 행
        Row rowSumDamageCrackTitle = sumSheet.createRow(SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE.row);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE, "파손", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_CRACK, "균열", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_OUTER_DAMAGE, "파손", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_OUTER_CRACK, "균열", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_INNER_DAMAGE, "파손", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_INNER_CRACK, "균열", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_PIPE_DAMAGE, "파손", hssfCellStyleBg);
        setSumStyleBg(sumSheet, rowSumDamageCrackTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_PIPE_CRACK, "균열", hssfCellStyleBg);

        //다섯번째 행
        Row rowSumValueTitle = sumSheet.createRow(SUM_CELL_STYLE.LADDER_Y.row);

        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LADDER_Y, "유", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LADDER_N, "무", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.INVERT_Y, "유", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.INVERT_N, "무", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_CRACK_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_CRACK_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_LID_CRACK_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_OUTER_DAMAGE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_OUTER_DAMAGE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_OUTER_DAMAGE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_OUTER_CRACK_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_OUTER_CRACK_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_OUTER_CRACK_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_INNER_DAMAGE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_INNER_DAMAGE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_INNER_DAMAGE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_INNER_CRACK_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_INNER_CRACK_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_INNER_CRACK_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_PIPE_DAMAGE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_PIPE_DAMAGE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_PIPE_DAMAGE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_PIPE_CRACK_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_PIPE_CRACK_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ABNORMAL_ITEM_MH_PIPE_CRACK_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_OUTER_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_OUTER_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_OUTER_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_INNER_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_INNER_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_INNER_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_PIPE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_PIPE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INVASION_PIPE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INNER_MH_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INNER_MH_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WATER_INNER_MH_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.OBSTACLE_G, "양호", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.OBSTACLE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.OBSTACLE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.OBSTACLE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ROOT_INTRUSION_G, "양호", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ROOT_INTRUSION_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ROOT_INTRUSION_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ROOT_INTRUSION_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ODOR_G, "양호", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ODOR_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ODOR_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ODOR_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LID_SAELING_Y, "여", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LID_SAELING_N, "부", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BURIED_Y, "유", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LADDER_DAMAGE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LADDER_DAMAGE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.LADDER_DAMAGE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ENDOTHELIUM_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ENDOTHELIUM_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.ENDOTHELIUM_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WASTEOIL_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WASTEOIL_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.WASTEOIL_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_GAP_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_GAP_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_GAP_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_DAMAGE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_DAMAGE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_DAMAGE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_LEAVE_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_LEAVE_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.BLOCK_LEAVE_S, "소", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.SURFACE_GAP_L, "대", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.SURFACE_GAP_M, "중", hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValueTitle, SUM_CELL_STYLE.SURFACE_GAP_S, "소", hssfCellStyleValue);

        //다섯번째 행
        Row rowSumValue = sumSheet.createRow(SUM_CELL_STYLE.VALUE_LOCAL.row);

        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LOCAL, fieldDetailListSumBeanS.getRes_mh_field_local(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_COUNT, fieldDetailListSumBeanS.getRes_mh_field_count(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LADDER_Y, fieldDetailListSumBeanS.getRes_mh_ladder_yn_y_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LADDER_N, fieldDetailListSumBeanS.getRes_mh_ladder_yn_n_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_INVERT_Y, fieldDetailListSumBeanS.getRes_mh_invert_yn_y_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_INVERT_N, fieldDetailListSumBeanS.getRes_mh_invert_yn_n_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE_L, fieldDetailListSumBeanS.getRes_mh_lid_damage_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE_M, fieldDetailListSumBeanS.getRes_mh_lid_damage_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE_S, fieldDetailListSumBeanS.getRes_mh_lid_damage_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_CRACK_L, fieldDetailListSumBeanS.getRes_mh_lid_crack_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_CRACK_M, fieldDetailListSumBeanS.getRes_mh_lid_crack_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_CRACK_S, fieldDetailListSumBeanS.getRes_mh_lid_crack_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_OUTER_DAMAGE_L, fieldDetailListSumBeanS.getRes_mh_outer_damage_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_OUTER_DAMAGE_M, fieldDetailListSumBeanS.getRes_mh_outer_damage_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_OUTER_DAMAGE_S, fieldDetailListSumBeanS.getRes_mh_outer_damage_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_OUTER_CRACK_L, fieldDetailListSumBeanS.getRes_mh_outer_crack_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_OUTER_CRACK_M, fieldDetailListSumBeanS.getRes_mh_outer_crack_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_OUTER_CRACK_S, fieldDetailListSumBeanS.getRes_mh_outer_crack_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_INNER_DAMAGE_L, fieldDetailListSumBeanS.getRes_mh_inner_damage_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_INNER_DAMAGE_M, fieldDetailListSumBeanS.getRes_mh_inner_damage_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_INNER_DAMAGE_S, fieldDetailListSumBeanS.getRes_mh_inner_damage_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_INNER_CRACK_L, fieldDetailListSumBeanS.getRes_mh_inner_crack_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_INNER_CRACK_M, fieldDetailListSumBeanS.getRes_mh_inner_crack_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_INNER_CRACK_S, fieldDetailListSumBeanS.getRes_mh_inner_crack_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_PIPE_DAMAGE_L, fieldDetailListSumBeanS.getRes_mh_pipe_damage_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_PIPE_DAMAGE_M, fieldDetailListSumBeanS.getRes_mh_pipe_damage_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_PIPE_DAMAGE_S, fieldDetailListSumBeanS.getRes_mh_pipe_damage_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_PIPE_CRACK_L, fieldDetailListSumBeanS.getRes_mh_pipe_crack_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_PIPE_CRACK_M, fieldDetailListSumBeanS.getRes_mh_pipe_crack_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_PIPE_CRACK_S, fieldDetailListSumBeanS.getRes_mh_pipe_crack_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_OUTER_L, fieldDetailListSumBeanS.getRes_mh_outer_water_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_OUTER_M, fieldDetailListSumBeanS.getRes_mh_outer_water_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_OUTER_S, fieldDetailListSumBeanS.getRes_mh_outer_water_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_INNER_L, fieldDetailListSumBeanS.getRes_mh_inner_water_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_INNER_M, fieldDetailListSumBeanS.getRes_mh_inner_water_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_INNER_S, fieldDetailListSumBeanS.getRes_mh_inner_water_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_PIPE_L, fieldDetailListSumBeanS.getRes_mh_pipe_water_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_PIPE_M, fieldDetailListSumBeanS.getRes_mh_pipe_water_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INVASION_PIPE_S, fieldDetailListSumBeanS.getRes_mh_pipe_water_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INNER_MH_L, fieldDetailListSumBeanS.getRes_mh_lid_water_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INNER_MH_M, fieldDetailListSumBeanS.getRes_mh_lid_water_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WATER_INNER_MH_S, fieldDetailListSumBeanS.getRes_mh_lid_water_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_OBSTACLE_G, fieldDetailListSumBeanS.getRes_mh_temp_obstacle_glms_g_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_OBSTACLE_L, fieldDetailListSumBeanS.getRes_mh_temp_obstacle_glms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_OBSTACLE_M, fieldDetailListSumBeanS.getRes_mh_temp_obstacle_glms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_OBSTACLE_S, fieldDetailListSumBeanS.getRes_mh_temp_obstacle_glms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ROOT_INTRUSION_G, fieldDetailListSumBeanS.getRes_mh_root_intrusion_glms_g_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ROOT_INTRUSION_L, fieldDetailListSumBeanS.getRes_mh_root_intrusion_glms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ROOT_INTRUSION_M, fieldDetailListSumBeanS.getRes_mh_root_intrusion_glms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ROOT_INTRUSION_S, fieldDetailListSumBeanS.getRes_mh_root_intrusion_glms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ODOR_G, fieldDetailListSumBeanS.getRes_mh_odor_glms_g_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ODOR_L, fieldDetailListSumBeanS.getRes_mh_odor_glms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ODOR_M, fieldDetailListSumBeanS.getRes_mh_odor_glms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ODOR_S, fieldDetailListSumBeanS.getRes_mh_odor_glms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LID_SAELING_Y, fieldDetailListSumBeanS.getRes_mh_lid_sealing_yn_y_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LID_SAELING_N, fieldDetailListSumBeanS.getRes_mh_lid_sealing_yn_n_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BURIED_Y, fieldDetailListSumBeanS.getRes_mh_buried_yn_y_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LADDER_DAMAGE_L, fieldDetailListSumBeanS.getRes_mh_ladder_damage_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LADDER_DAMAGE_M,  fieldDetailListSumBeanS.getRes_mh_ladder_damage_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_LADDER_DAMAGE_S,  fieldDetailListSumBeanS.getRes_mh_ladder_damage_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ENDOTHELIUM_L,  fieldDetailListSumBeanS.getRes_mh_endothelium_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ENDOTHELIUM_M, fieldDetailListSumBeanS.getRes_mh_endothelium_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_ENDOTHELIUM_S, fieldDetailListSumBeanS.getRes_mh_endothelium_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WASTEOIL_L, fieldDetailListSumBeanS.getRes_mh_wasteoil_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WASTEOIL_M, fieldDetailListSumBeanS.getRes_mh_wasteoil_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_WASTEOIL_S, fieldDetailListSumBeanS.getRes_mh_wasteoil_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_GAP_L, fieldDetailListSumBeanS.getRes_mh_block_gap_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_GAP_M, fieldDetailListSumBeanS.getRes_mh_block_gap_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_GAP_S, fieldDetailListSumBeanS.getRes_mh_block_gap_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_DAMAGE_L, fieldDetailListSumBeanS.getRes_mh_block_damage_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_DAMAGE_M, fieldDetailListSumBeanS.getRes_mh_block_damage_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_DAMAGE_S, fieldDetailListSumBeanS.getRes_mh_block_damage_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_LEAVE_L, fieldDetailListSumBeanS.getRes_mh_block_leave_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_LEAVE_M, fieldDetailListSumBeanS.getRes_mh_block_leave_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_BLOCK_LEAVE_S, fieldDetailListSumBeanS.getRes_mh_block_leave_lms_s_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_SURFACE_GAP_L, fieldDetailListSumBeanS.getRes_mh_surface_gap_lms_l_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_SURFACE_GAP_M, fieldDetailListSumBeanS.getRes_mh_surface_gap_lms_m_cnt(), hssfCellStyleValue);
        setSumStyle(sumSheet, rowSumValue, SUM_CELL_STYLE.VALUE_SURFACE_GAP_S, fieldDetailListSumBeanS.getRes_mh_surface_gap_lms_s_cnt(), hssfCellStyleValue);

        //우측 라인끗기
        //HSSFCellStyle hssfCellStyleRight = createCellStyleBorderRight();

        HSSFCellStyle hssfCellStyleRight = mWorkbook.createCellStyle();
        hssfCellStyleRight.setBorderRight((short)1);
        //Cell alignment 지정하기
        hssfCellStyleRight.setAlignment(CellStyle.ALIGN_CENTER);
        hssfCellStyleRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        HashMap<Integer, SUM_CELL_STYLE> sumCellStyleHashMap = new HashMap<Integer, SUM_CELL_STYLE>();
        sumCellStyleHashMap.put(1, SUM_CELL_STYLE.OBSTACLE);
        sumCellStyleHashMap.put(2, SUM_CELL_STYLE.ROOT_INTRUSION);
        sumCellStyleHashMap.put(3, SUM_CELL_STYLE.ODOR);
        sumCellStyleHashMap.put(4, SUM_CELL_STYLE.LID_SEALING);
        sumCellStyleHashMap.put(5, SUM_CELL_STYLE.BURIED);
        sumCellStyleHashMap.put(6, SUM_CELL_STYLE.LADDER_DAMAGE);
        sumCellStyleHashMap.put(7, SUM_CELL_STYLE.ENDOTHELIUM);
        sumCellStyleHashMap.put(8, SUM_CELL_STYLE.WASTEOIL);
        sumCellStyleHashMap.put(9, SUM_CELL_STYLE.BLOCK_GAP);
        sumCellStyleHashMap.put(10, SUM_CELL_STYLE.BLOCK_DAMAGE);
        sumCellStyleHashMap.put(11, SUM_CELL_STYLE.BLOCK_LEAVE);
        sumCellStyleHashMap.put(12, SUM_CELL_STYLE.SURFACE_GAP);

        //row 2,3에 대해서 우측 라인 긋기
        for(int k = 2; k < 4; k++) {
            Row iRow = sumSheet.getRow(k);
            if(iRow == null){
                iRow = sumSheet.createRow(k);
            }

            for (Map.Entry<Integer, SUM_CELL_STYLE> entry : sumCellStyleHashMap.entrySet()) {
                SUM_CELL_STYLE sumCellStyle = entry.getValue();

                for (int i = sumCellStyle.firstCol; i <= sumCellStyle.lastCol; i++) {
                    Cell iCell;
                    try {
                        iCell = iRow.getCell(i);
                        if (iCell == null) {
                            iCell = iRow.createCell(i);
                        }
                    } catch (NullPointerException e) {
                        iCell = iRow.createCell(i);
                    }
                    iCell.setCellStyle(hssfCellStyleRight);
                }
            }
        }

        Row rowDiv3 = sumSheet.getRow(3);
        if(rowDiv3 == null){
            rowDiv3 = sumSheet.createRow(3);
        }

        HashMap<Integer, SUM_CELL_STYLE> sumCellStyleHashMap_water = new HashMap<Integer, SUM_CELL_STYLE>();
        sumCellStyleHashMap_water.put(1, SUM_CELL_STYLE.MH_WATER_INVASION_OUTER);
        sumCellStyleHashMap_water.put(2, SUM_CELL_STYLE.MH_WATER_INVASION_INNER);
        sumCellStyleHashMap_water.put(3, SUM_CELL_STYLE.MH_WATER_INVASION_PIPE);
        sumCellStyleHashMap_water.put(4, SUM_CELL_STYLE.MH_WATER_INNER_LID);

        for (Map.Entry<Integer, SUM_CELL_STYLE> entry : sumCellStyleHashMap_water.entrySet()) {
            SUM_CELL_STYLE sumCellStyle = entry.getValue();

            for (int i = sumCellStyle.firstCol; i <= sumCellStyle.lastCol; i++) {
                Cell iCell;
                try {
                    iCell = rowDiv3.getCell(i);
                    if (iCell == null) {
                        iCell = rowDiv3.createCell(i);
                    }
                } catch (NullPointerException e) {
                    iCell = rowDiv3.createCell(i);
                }
                iCell.setCellStyle(hssfCellStyleRight);
            }
        }

        //처리분구, 현장조사개수(개소), 사다리설치에 대한 우측 라인 긋기, 2,3라인
        HashMap<Integer, SUM_CELL_STYLE> sumCellStyleHashMap_local = new HashMap<Integer, SUM_CELL_STYLE>();
        sumCellStyleHashMap_local.put(1, SUM_CELL_STYLE.LOCAL);
        sumCellStyleHashMap_local.put(2, SUM_CELL_STYLE.COUNT);
        sumCellStyleHashMap_local.put(3, SUM_CELL_STYLE.LADDER_YN);

        //row 2,3에 대해서 우측 라인 긋기
        for(int k = 2; k < 5; k++) {
            Row iRow = sumSheet.getRow(k);
            if(iRow == null){
                iRow = sumSheet.createRow(k);
            }

            for (Map.Entry<Integer, SUM_CELL_STYLE> entry : sumCellStyleHashMap_local.entrySet()) {
                SUM_CELL_STYLE sumCellStyle = entry.getValue();

                for (int i = sumCellStyle.firstCol; i <= sumCellStyle.lastCol; i++) {
                    Cell iCell;
                    try {
                        iCell = iRow.getCell(i);
                        if (iCell == null) {
                            iCell = iRow.createCell(i);
                        }
                    } catch (NullPointerException e) {
                        iCell = iRow.createCell(i);
                    }
                    iCell.setCellStyle(hssfCellStyleRight);
                }
            }
        }

        //유무 및 데이타 >>>
        for(int k = 4; k < 6; k++) {
            Row iRow = sumSheet.getRow(k);
            if (iRow == null) {
                iRow = sumSheet.createRow(k);
            }

            for(int j = 0; j < 78; j++) {
                Cell iCell;
                try {
                    iCell = iRow.getCell(j);
                    if (iCell == null) {
                        iCell = iRow.createCell(j);
                    }
                } catch (NullPointerException e) {
                    iCell = iRow.createCell(j);
                }
                iCell.setCellStyle(hssfCellStyleValue);
            }
        }

        setColumeWidth(sumSheet, SHEET_TYPE.SUMMARY);

        createManholeSumDetailExcel(fieldBasicBeanS, mFieldDetailList);

    }

    /**
     * 엑셀 생성 > 현장조사 집계표 상세항목
     */
    public void createManholeSumDetailExcel(FieldBasicBeanS fieldBasicBeanS, ArrayList<FieldDetailBeanS> fieldDetailList){
        GomsLog.d(LOG_TAG, "createManholeSumExcel()");

        HSSFSheet sumDetailSheet = createSumDetailSheet("현장조사 집계표");

        /*
        //row 5, col 30까지 생성
        int total = SUM_DETAIL_CELL_STYLE.VALUE_NO.row + fieldDetailList.size() + 1;
        GomsLog.d(LOG_TAG, "createManholeSumExcel() rowTotal : " + total);
        GomsLog.d(LOG_TAG, "createManholeSumExcel() colTotal : " + SUM_DETAIL_CELL_STYLE.SUM_DETAIL_MAIN_TITLE.lastCol);
        for(int rowIndex = 0; rowIndex < total; rowIndex++){
            Row row = createRow(sumDetailSheet, rowIndex);
            for (int colIndex = 0; colIndex <= SUM_DETAIL_CELL_STYLE.SUM_DETAIL_MAIN_TITLE.lastCol; colIndex++) {  //30까지
                row.createCell(colIndex);
            }
        }
        */

        //첫번째 행 >> 현장조사 집계표

        Font fontMainTitle = mWorkbook.createFont();
        fontMainTitle.setBold(true);
        fontMainTitle.setFontHeight(TITLE_SIZE.SUM_TITLE.size);
        fontMainTitle.setUnderline(U_NONE);

        //CellStyle 객체생성 - 메인타이틀
        HSSFCellStyle hssfCellStyleMainTitle = createCellStyle();
        //Cell alignment 지정하기
        short hAlignCenter = CellStyle.ALIGN_CENTER;
        short vAlignCenter = CellStyle.VERTICAL_CENTER;
        hssfCellStyleMainTitle.setAlignment(hAlignCenter);
        hssfCellStyleMainTitle.setVerticalAlignment(vAlignCenter);
        hssfCellStyleMainTitle.setFont(fontMainTitle);

        Row rowSumMainTitle = sumDetailSheet.createRow(SUM_DETAIL_CELL_STYLE.SUM_DETAIL_MAIN_TITLE.row);
        setSumDetailMainTitleStyle(sumDetailSheet, rowSumMainTitle, SUM_DETAIL_CELL_STYLE.SUM_DETAIL_MAIN_TITLE, "현장조사 집계표", hssfCellStyleMainTitle);

        Font font = getFont();

        Font fontReportBg = mWorkbook.createFont();
        fontReportBg.setBold(false);
        fontReportBg.setFontHeight(TITLE_SIZE.TITLE.size);
        fontReportBg.setUnderline(U_NONE);

        //CellStyle 객체생성 - 배경
        HSSFCellStyle hssfCellStyleBg = createCellStyle();
        hssfCellStyleBg.setAlignment(hAlignCenter);
        hssfCellStyleBg.setVerticalAlignment(vAlignCenter);
        hssfCellStyleBg.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillPattern((short) 1);
        hssfCellStyleBg.setBorderTop((short)1);
        hssfCellStyleBg.setBorderBottom((short)1);
        hssfCellStyleBg.setBorderLeft((short)1);
        hssfCellStyleBg.setBorderRight((short)1);
        hssfCellStyleBg.setFont(fontReportBg);

        HSSFCellStyle hssfCellStyle = createCellStyle();
        hssfCellStyle.setAlignment(hAlignCenter);
        hssfCellStyle.setVerticalAlignment(vAlignCenter);
        hssfCellStyle.setBorderTop((short)1);
        hssfCellStyle.setBorderBottom((short)1);
        hssfCellStyle.setBorderLeft((short)1);
        hssfCellStyle.setBorderRight((short)1);
        hssfCellStyle.setFont(fontReportBg);

        //두번째 행
        Row rowSumTitle = sumDetailSheet.createRow(SUM_DETAIL_CELL_STYLE.LOCAL.row);
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.NO, "야장\n쪽수");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.LOCAL, "처리\n분구");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.MH_NUM, "현장번호");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.MH_DRAINAGE, "배제방식");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.LADDER_YN, "사다리설치");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.INVERT_YN, "인버트설치");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM, "이상항목");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.WATER_INVASION, "침입수");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.WATER_INNER, "유입수");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.OBSTACLE, "장애물");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.ROOT_INTRUSION, "뿌리침입");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.ODOR, "악취발생");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.LID_SEALING, "뚜껑밀폐");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.BURIED, "매몰");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.LADDER_DAMAGE, "사다리손상");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.ENDOTHELIUM, "내피생성");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.WASTEOIL, "폐유부착");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.BLOCK_GAP, "블록이음부단차");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE, "블록이음부손상");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE, "블록이음부이탈");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.SURFACE_GAP, "표면단차");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumTitle, SUM_DETAIL_CELL_STYLE.MH_REMARK, "특이사항");


        //세번째 행
        Row rowSumSubTitle = sumDetailSheet.createRow(SUM_DETAIL_CELL_STYLE.MH_LID.row);
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_LID, "뚜껑");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_OUTER, "현장주변부");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_INNER, "현장내부");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_PIPE, "관로접합부");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_WATER_INVASION_OUTER, "현장주변부");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_WATER_INVASION_INNER, "현장내부");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_WATER_INVASION_PIPE, "관로접합부");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumSubTitle, SUM_DETAIL_CELL_STYLE.MH_WATER_INNER_LID, "뚜껑");

        //네번째 행
        Row rowSumDamageCrackTitle = sumDetailSheet.createRow(SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE.row);
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_DAMAGE, "파손");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_CRACK, "균열");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_OUTER_DAMAGE, "파손");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_OUTER_CRACK, "균열");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_INNER_DAMAGE, "파손");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_INNER_CRACK, "균열");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_PIPE_DAMAGE, "파손");
        setSumDetailStyleBg(sumDetailSheet, hssfCellStyleBg, rowSumDamageCrackTitle, SUM_DETAIL_CELL_STYLE.ABNORMAL_ITEM_MH_LID_PIPE_CRACK, "균열");

        //집계표 상세 > 계 > 해당 총합계
        int fieldNum = 0;   //No.
        int sum_ladder_yn = 0;
        int sum_invert_yn = 0;
        int sum_abnormal_item_mh_lid_damage = 0;
        int sum_abnormal_item_mh_lid_crack = 0;
        int sum_abnormal_item_mh_lid_outer_damage = 0;
        int sum_abnormal_item_mh_lid_outer_crack = 0;
        int sum_abnormal_item_mh_lid_inner_damage = 0;
        int sum_abnormal_item_mh_lid_inner_crack = 0;
        int sum_abnormal_item_mh_lid_pipe_damage = 0;
        int sum_abnormal_item_mh_lid_pipe_crack = 0;
        int sum_mh_water_invasion_outer = 0;
        int sum_mh_water_invasion_inner = 0;
        int sum_mh_water_invasion_pipe = 0;
        int sum_mh_water_inner_lid = 0;
        int sum_obstacle = 0;
        int sum_root_intrusion = 0;
        int sum_odor = 0;
        int sum_lid_sealing_yn = 0;
        int sum_buried_yn = 0;
        int sum_ladder_damage = 0;
        int sum_endothelium = 0;
        int sum_wasteoil = 0;
        int sum_block_gap = 0;
        int sum_block_damage = 0;
        int sum_block_leave = 0;
        int sum_surface_gap = 0;
        int sum_mh_remark = 0;

        //야장쪽수, 처리구분
        for(FieldDetailBeanS fieldDetailBeanS : fieldDetailList) {

            //다섯번째 행부터 Value 행입니다.
            int rowNum = fieldNum + SUM_DETAIL_CELL_STYLE.VALUE_NO.row;
            Row rowSumValue = sumDetailSheet.createRow(rowNum);

            //해당 값을 넣음.
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_NO, StringUtil.intToString(fieldNum+1));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_LOCAL, fieldBasicBeanS.getRes_mh_field_local());
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_NUM, fieldDetailBeanS.getRes_mh_num());
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_DRAINAGE, fieldDetailBeanS.getRes_mh_drainage());
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_LADDER_YN, convertData(fieldDetailBeanS.getRes_mh_ladder_yn()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_INVERT_YN, convertData(fieldDetailBeanS.getRes_mh_invert_yn()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_lid_damage_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_CRACK, convertData(fieldDetailBeanS.getRes_mh_lid_crack_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_OUTER_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_outer_damage_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_OUTER_CRACK, convertData(fieldDetailBeanS.getRes_mh_outer_crack_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_INNER_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_inner_damage_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_INNER_CRACK, convertData(fieldDetailBeanS.getRes_mh_inner_crack_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_PIPE_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_pipe_damage_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ABNORMAL_ITEM_MH_LID_PIPE_CRACK, convertData(fieldDetailBeanS.getRes_mh_pipe_crack_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_WATER_INVASION_OUTER, convertData(fieldDetailBeanS.getRes_mh_outer_water_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_WATER_INVASION_INNER, convertData(fieldDetailBeanS.getRes_mh_inner_water_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_WATER_INVASION_PIPE, convertData(fieldDetailBeanS.getRes_mh_pipe_water_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_WATER_INNER_LID, convertData(fieldDetailBeanS.getRes_mh_lid_water_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_OBSTACLE, convertData(fieldDetailBeanS.getRes_mh_temp_obstacle_glms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ROOT_INTRUSION, convertData(fieldDetailBeanS.getRes_mh_root_intrusion_glms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ODOR, convertData(fieldDetailBeanS.getRes_mh_odor_glms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_LID_SEALING_YN, convertData(fieldDetailBeanS.getRes_mh_lid_sealing_yn()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_BURIED_YN, convertData(fieldDetailBeanS.getRes_mh_buried_yn()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_LADDER_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_ladder_damage_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_ENDOTHELIUM, convertData(fieldDetailBeanS.getRes_mh_endothelium_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_WASTEOIL, convertData(fieldDetailBeanS.getRes_mh_wasteoil_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_BLOCK_GAP, convertData(fieldDetailBeanS.getRes_mh_block_gap_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_BLOCK_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_block_damage_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_BLOCK_LEAVE, convertData(fieldDetailBeanS.getRes_mh_block_leave_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_SURFACE_GAP, convertData(fieldDetailBeanS.getRes_mh_surface_gap_lms()));
            setSumDetailStyle(sumDetailSheet, font, rowSumValue, SUM_DETAIL_CELL_STYLE.VALUE_MH_REMARK, convertData(fieldDetailBeanS.getRes_mh_remark()));

            //합계 더하기 처리
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_ladder_yn())) sum_ladder_yn++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_invert_yn())) sum_invert_yn++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_lid_damage_lms())) sum_abnormal_item_mh_lid_damage++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_lid_crack_lms())) sum_abnormal_item_mh_lid_crack++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_outer_damage_lms())) sum_abnormal_item_mh_lid_outer_damage++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_outer_crack_lms())) sum_abnormal_item_mh_lid_outer_crack++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_inner_damage_lms())) sum_abnormal_item_mh_lid_inner_damage++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_inner_crack_lms())) sum_abnormal_item_mh_lid_inner_crack++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_pipe_damage_lms())) sum_abnormal_item_mh_lid_pipe_damage++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_pipe_crack_lms())) sum_abnormal_item_mh_lid_pipe_crack++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_outer_water_lms())) sum_mh_water_invasion_outer++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_inner_water_lms())) sum_mh_water_invasion_inner++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_pipe_water_lms())) sum_mh_water_invasion_pipe++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_lid_water_lms()))  sum_mh_water_inner_lid++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_temp_obstacle_glms())) sum_obstacle++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_root_intrusion_glms())) sum_root_intrusion++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_odor_glms())) sum_odor++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_lid_sealing_yn())) sum_lid_sealing_yn++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_buried_yn())) sum_buried_yn++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_ladder_damage_lms())) sum_ladder_damage++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_endothelium_lms())) sum_endothelium++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_wasteoil_lms())) sum_wasteoil++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_block_gap_lms())) sum_block_gap++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_block_damage_lms())) sum_block_damage++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_block_leave_lms())) sum_block_leave++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_surface_gap_lms())) sum_surface_gap++;
            if(StringUtil.isNotNull(fieldDetailBeanS.getRes_mh_remark())) sum_mh_remark++;

            fieldNum++;
        }

        //마지막 행(계) 부분을 처리함.
        int rowFinal = fieldNum + SUM_DETAIL_CELL_STYLE.VALUE_NO.row;
        GomsLog.d(LOG_TAG, "createManholeSumExcel() last rowFinal : " + rowFinal);

        Row rowSumValueFinal = sumDetailSheet.createRow(rowFinal);
        setSumDetailSummaryStyleBg(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 0, 3, "계", hssfCellStyleBg);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 4,4,StringUtil.intToString(sum_ladder_yn), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 5,5,StringUtil.intToString(sum_invert_yn), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 6,6,StringUtil.intToString(sum_abnormal_item_mh_lid_damage), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 7,7,StringUtil.intToString(sum_abnormal_item_mh_lid_crack), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 8,8,StringUtil.intToString(sum_abnormal_item_mh_lid_outer_damage), hssfCellStyle);;
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 9,9,StringUtil.intToString(sum_abnormal_item_mh_lid_outer_crack), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 10,10,StringUtil.intToString(sum_abnormal_item_mh_lid_inner_damage), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 11,11,StringUtil.intToString(sum_abnormal_item_mh_lid_inner_crack), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 12,12,StringUtil.intToString(sum_abnormal_item_mh_lid_pipe_damage), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 13,13,StringUtil.intToString(sum_abnormal_item_mh_lid_pipe_crack), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 14,14,StringUtil.intToString(sum_mh_water_invasion_outer), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 15,15,StringUtil.intToString(sum_mh_water_invasion_inner), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 16,16,StringUtil.intToString(sum_mh_water_invasion_pipe), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 17,17,StringUtil.intToString(sum_mh_water_inner_lid), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 18,18,StringUtil.intToString(sum_obstacle), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 19,19,StringUtil.intToString(sum_root_intrusion), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 20,20,StringUtil.intToString(sum_odor), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 21,21,StringUtil.intToString(sum_lid_sealing_yn), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 22,22,StringUtil.intToString(sum_buried_yn), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 23,23,StringUtil.intToString(sum_ladder_damage), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 24,24,StringUtil.intToString(sum_endothelium), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 25,25,StringUtil.intToString(sum_wasteoil), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 26,26,StringUtil.intToString(sum_block_gap), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 27,27,StringUtil.intToString(sum_block_damage), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 28,28,StringUtil.intToString(sum_block_leave), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 29,29,StringUtil.intToString(sum_surface_gap), hssfCellStyle);
        setSumDetailSummaryStyle(sumDetailSheet, rowSumValueFinal, rowFinal, rowFinal, 30,30,StringUtil.intToString(sum_mh_remark), hssfCellStyle);

        //데이타부분에 스타일 넣기
        setDataCellStyle(sumDetailSheet, SUM_DETAIL_CELL_STYLE.VALUE_NO.row, rowFinal, SUM_DETAIL_CELL_STYLE.SUM_DETAIL_MAIN_TITLE.firstCol, SUM_DETAIL_CELL_STYLE.SUM_DETAIL_MAIN_TITLE.lastCol);

        setColumeWidth(sumDetailSheet, SHEET_TYPE.SUMMARY_DETAIL);


        //현장내부 관로접합부 파손,균열 색상 및 데이타 지정
/*        HSSFCellStyle hssfCellStyle = createCellStyle();
        hssfCellStyle.setAlignment(hAlignCenter);
        hssfCellStyle.setVerticalAlignment(vAlignCenter);
        hssfCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyle.setFillPattern((short) 1);
        hssfCellStyle.setBorderLeft((short)1);
        hssfCellStyle.setBorderRight((short)1);
        hssfCellStyle.setFont(getFont());*/

        /*
        Row row3 = sumDetailSheet.getRow(3);
        for(int i = 10; i < 14;i++) {
            Cell cell = row3.getCell(i);
            cell.setCellStyle(hssfCellStyle);
        }*/

        //HSSFCellStyle hssfCellStyleRight = createCellStyleBorderRight();
        HSSFCellStyle hssfCellStyleRight = mWorkbook.createCellStyle();
        hssfCellStyleRight.setBorderRight((short)1);
        //Cell alignment 지정하기
        hssfCellStyleRight.setAlignment(CellStyle.ALIGN_CENTER);
        hssfCellStyleRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        //침입수, 유입수 > 현장주변부, 현장내부, 관로접합부,뚜껑 우측라인 끗기
        Row rowDiv3 = sumDetailSheet.getRow(3);
        if(rowDiv3 == null){
            rowDiv3= sumDetailSheet.createRow(3);
        }

        for(int i = 14; i < 18; i++){
            Cell iCell;
            try {
                iCell = rowDiv3.getCell(i);
                if (iCell == null) {
                    iCell = rowDiv3.createCell(i);
                }
            }catch (NullPointerException e){
                iCell = rowDiv3.createCell(i);
            }
            iCell.setCellStyle(hssfCellStyleRight);
        }

        HashMap<Integer, SUM_DETAIL_CELL_STYLE> sumCellStyleHashMap = new HashMap<Integer, SUM_DETAIL_CELL_STYLE>();
        sumCellStyleHashMap.put(1, SUM_DETAIL_CELL_STYLE.NO);
        sumCellStyleHashMap.put(2, SUM_DETAIL_CELL_STYLE.LOCAL);
        sumCellStyleHashMap.put(3, SUM_DETAIL_CELL_STYLE.MH_NUM);
        sumCellStyleHashMap.put(4, SUM_DETAIL_CELL_STYLE.MH_DRAINAGE);
        sumCellStyleHashMap.put(5, SUM_DETAIL_CELL_STYLE.LADDER_YN);
        sumCellStyleHashMap.put(6, SUM_DETAIL_CELL_STYLE.INVERT_YN);
        sumCellStyleHashMap.put(7, SUM_DETAIL_CELL_STYLE.OBSTACLE);
        sumCellStyleHashMap.put(8, SUM_DETAIL_CELL_STYLE.ROOT_INTRUSION);
        sumCellStyleHashMap.put(9, SUM_DETAIL_CELL_STYLE.ODOR);
        sumCellStyleHashMap.put(10, SUM_DETAIL_CELL_STYLE.LID_SEALING);
        sumCellStyleHashMap.put(11, SUM_DETAIL_CELL_STYLE.BURIED);
        sumCellStyleHashMap.put(12, SUM_DETAIL_CELL_STYLE.LADDER_DAMAGE);
        sumCellStyleHashMap.put(13, SUM_DETAIL_CELL_STYLE.ENDOTHELIUM);
        sumCellStyleHashMap.put(14, SUM_DETAIL_CELL_STYLE.WASTEOIL);
        sumCellStyleHashMap.put(15, SUM_DETAIL_CELL_STYLE.BLOCK_GAP);
        sumCellStyleHashMap.put(16, SUM_DETAIL_CELL_STYLE.BLOCK_DAMAGE);
        sumCellStyleHashMap.put(17, SUM_DETAIL_CELL_STYLE.BLOCK_LEAVE);
        sumCellStyleHashMap.put(18, SUM_DETAIL_CELL_STYLE.SURFACE_GAP);
        sumCellStyleHashMap.put(19, SUM_DETAIL_CELL_STYLE.MH_REMARK);

        //row 2,3에 대해서 우측 라인 긋기
        for(int k = 2; k < 4; k++) {
            Row iRow = sumDetailSheet.getRow(k);
            if(iRow == null){
                iRow = sumDetailSheet.createRow(k);
            }

            for (Map.Entry<Integer, SUM_DETAIL_CELL_STYLE> entry : sumCellStyleHashMap.entrySet()) {
                SUM_DETAIL_CELL_STYLE sumCellStyle = entry.getValue();

                for (int i = sumCellStyle.firstCol; i <= sumCellStyle.lastCol; i++) {
                    Cell iCell;
                    try {
                        iCell = iRow.getCell(i);
                        if (iCell == null) {
                            iCell = iRow.createCell(i);
                        }
                    } catch (NullPointerException e) {
                        iCell = iRow.createCell(i);
                    }
                    iCell.setCellStyle(hssfCellStyleRight);
                }
            }
        }

        createManholeSumExtentionExcel(fieldBasicBeanS, fieldDetailList);
        //saveExcel();
    }

    /**
     * 엑셀 생성 > 현장조사 연장집계표
     */
    public void createManholeSumExtentionExcel(FieldBasicBeanS fieldBasicBeanS, ArrayList<FieldDetailBeanS> fieldDetailList){
        GomsLog.d(LOG_TAG, "createManholeSumExtensionExcel() >>> 현장조사 연장집계표");

        HSSFSheet sumExtensionSheet = createSumExtensionSheet("연장집계표");

        //row 9, col 9까지 생성
        int rowTotal = SUM_EXTENSION_CELL_STYLE.VALUE_NO.row + fieldDetailList.size();
        //GomsLog.d(LOG_TAG, "createManholeSumExtentionExcel() rowTotal : " + rowTotal);
        //GomsLog.d(LOG_TAG, "createManholeSumExtentionExcel() colTotal : " + SUM_EXTENSION_CELL_STYLE.MAIN_TITLE.lastCol);

        /*
        해당 건이 있으면 집계표 제목이 날라갑니다. 주석처리 20230822
        for(int rowExIndex = 0; rowExIndex < rowTotal; rowExIndex++){
            Row rowEx = createRow(sumExtensionSheet, rowExIndex);
            for (int colExIndex = 0; colExIndex <= SUM_EXTENSION_CELL_STYLE.MAIN_TITLE.lastCol; colExIndex++) {  //11까지
                rowEx.createCell(colExIndex);
            }
        }
        */

        //첫번째 행 >> 연장집계표

        Font fontMainTitle = mWorkbook.createFont();
        fontMainTitle.setBold(true);
        fontMainTitle.setFontHeight(TITLE_SIZE.SUM_TITLE.size);
        fontMainTitle.setUnderline(U_NONE);

        //CellStyle 객체생성 - 메인타이틀
        HSSFCellStyle hssfCellStyleMainTitle = createCellStyle();
        //Cell alignment 지정하기
        short hAlignCenter = CellStyle.ALIGN_CENTER;
        short vAlignCenter = CellStyle.VERTICAL_CENTER;
        hssfCellStyleMainTitle.setAlignment(hAlignCenter);
        hssfCellStyleMainTitle.setVerticalAlignment(vAlignCenter);
        hssfCellStyleMainTitle.setFont(fontMainTitle);

        Row rowSumMainTitle = sumExtensionSheet.createRow(SUM_EXTENSION_CELL_STYLE.MAIN_TITLE.row);
        setExtensionMainTitleStyle(sumExtensionSheet, rowSumMainTitle, SUM_EXTENSION_CELL_STYLE.MAIN_TITLE, "연장집계표", hssfCellStyleMainTitle);


        Font fontReportBg = mWorkbook.createFont();
        fontReportBg.setBold(false);
        fontReportBg.setFontHeight(TITLE_SIZE.TITLE.size);
        fontReportBg.setUnderline(U_NONE);

        //CellStyle 객체생성 - 배경
        HSSFCellStyle hssfCellStyleBg = createCellStyle();
        hssfCellStyleBg.setAlignment(hAlignCenter);
        hssfCellStyleBg.setVerticalAlignment(vAlignCenter);
        hssfCellStyleBg.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillPattern((short) 1);
        hssfCellStyleBg.setBorderTop((short)1);
        hssfCellStyleBg.setBorderBottom((short)1);
        hssfCellStyleBg.setBorderLeft((short)1);
        hssfCellStyleBg.setBorderRight((short)1);
        hssfCellStyleBg.setFont(fontReportBg);

        //두번째 행
        Row rowSumTitle = sumExtensionSheet.createRow(SUM_EXTENSION_CELL_STYLE.LOCAL.row);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.NO, "연번", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_DATE, "조사일", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.LOCAL, "처리\n분구", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_NUM, "현장번호", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_DRAINAGE, "배제방식", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_LOCAL_SPACIES, "관종", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_LOCAL_CIRCUMFERENCT, "관경", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_LOCAL_EXTENSION, "연장", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumTitle, SUM_EXTENSION_CELL_STYLE.MH_LOCAL_BIGO, "비고", hssfCellStyleBg);

        //세번째 행
        Row rowSumSubTitle = sumExtensionSheet.createRow(SUM_EXTENSION_CELL_STYLE.MH_LOCAL_SP.row);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumSubTitle, SUM_EXTENSION_CELL_STYLE.MH_LOCAL_SP, "시점", hssfCellStyleBg);
        setSumExtensionStyleBg(sumExtensionSheet, rowSumSubTitle, SUM_EXTENSION_CELL_STYLE.MH_LOCAL_EP, "종점", hssfCellStyleBg);

        //연장집계표 상세 > 데이타 시작행 확인 및 해당 데이타 넣기
        int fieldNum = 0;   //No.

        //연장집계표 > 데이타 넣기 및 스타일 적용
        for(FieldDetailBeanS fieldDetailBeanS : fieldDetailList) {

            //다섯번째 행부터 Value 행입니다.
            int rowNum = fieldNum + SUM_EXTENSION_CELL_STYLE.VALUE_NO.row;
            Row rowSumExtesionValue = sumExtensionSheet.createRow(rowNum);

            //해당 값을 넣음.
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_NO, StringUtil.intToString(fieldNum+1));
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_DATE, fieldDetailBeanS.getRes_mh_date());
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_LOCAL, fieldBasicBeanS.getRes_mh_field_local());
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_LOCAL_SP, fieldDetailBeanS.getRes_mh_local_sp());
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_LOCAL_EP, fieldDetailBeanS.getRes_mh_local_ep());
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_DRAINAGE, convertData(fieldDetailBeanS.getRes_mh_drainage()));
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_LOCAL_SPACIES, convertData(fieldDetailBeanS.getRes_mh_local_species()));
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_LOCAL_CIRCUMFERENCE, convertData(fieldDetailBeanS.getRes_mh_local_circumference()));
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_LOCAL_EXTENTION, convertData(fieldDetailBeanS.getRes_mh_local_extension()));
            setSumExtensionStyle(sumExtensionSheet, rowSumExtesionValue, SUM_EXTENSION_CELL_STYLE.VALUE_MH_LOCAL_BIGO, convertData(fieldDetailBeanS.getRes_mh_local_bigo()));
            fieldNum++;
        }

        //스타일 지정. 우측 라인 없을 시, 아래 지정해서 우측 라인을 끗는다.
        HSSFCellStyle hssfCellStyleRight = createCellStyle();
        hssfCellStyleRight.setBorderLeft((short)1);
        hssfCellStyleRight.setBorderRight((short)1);

        //우측 라인 넣기
        Row rowDiv2 = sumExtensionSheet.getRow(2);
        if(rowDiv2 == null){
            rowDiv2= sumExtensionSheet.createRow(2);
        }

        //연번 및 현장번호 시점 앞까지 열
        for(int i = 0; i < 3; i++){
            Cell iCell;
            try {
                iCell = rowDiv2.getCell(i);
                if (iCell == null) {
                    iCell = rowDiv2.createCell(i);
                }
            }catch (NullPointerException e){
                iCell = rowDiv2.createCell(i);
            }
            iCell.setCellStyle(hssfCellStyleRight);
        }

        //현장번호 종점 이후부터 끝까지
        for(int i = 5; i < 10; i++){
            Cell iCell;
            try {
                iCell = rowDiv2.getCell(i);
                if (iCell == null) {
                    iCell = rowDiv2.createCell(i);
                }
            }catch (NullPointerException e){
                iCell = rowDiv2.createCell(i);
            }
            iCell.setCellStyle(hssfCellStyleRight);
        }
        //우측라인넣기 끝..

        //마지막 행(계) 부분을 처리함.
        int rowFinal = fieldNum + SUM_EXTENSION_CELL_STYLE.VALUE_NO.row;
        GomsLog.d(LOG_TAG, "createManholeSumExtensionExcel() last rowFinal : " + rowFinal);

        //데이타부분에 스타일 넣기
        setDataExtensionCellStyle(sumExtensionSheet, SUM_EXTENSION_CELL_STYLE.VALUE_NO.row, rowFinal, SUM_EXTENSION_CELL_STYLE.MAIN_TITLE.firstCol, SUM_EXTENSION_CELL_STYLE.MAIN_TITLE.lastCol);

        //연장집계표 센터 정렬
        setPaperAligment(sumExtensionSheet);
        setColumeWidth(sumExtensionSheet, SHEET_TYPE.SUMMARY_EXTENSION);

        //드디어, 마지막 현장야장조사 엑셀만들기
        createManholeExcel(fieldBasicBeanS, fieldDetailList);

        //saveExcel();

    }

    /**
     * 현장조사집계표 > 데이타 부분 스타일
     */
    private void setDataCellStyle(HSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol){

        short fontHeight = (short) 110;

        boolean bold = false;
        byte underline = U_NONE;

        //CellStyle 객체생성
        //HSSFCellStyle hssfCellStyle = createCellStyleBorderAll();
        HSSFCellStyle hssfCellStyle = mWorkbook.createCellStyle();
        hssfCellStyle.setBorderTop((short)1);
        hssfCellStyle.setBorderBottom((short)1);
        hssfCellStyle.setBorderRight((short)1);
        hssfCellStyle.setBorderLeft((short)1);
        //Cell alignment 지정하기
        hssfCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        hssfCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        //Font 객체생성
        Font font = getFontDataType();

        font.setBold(bold);
        font.setFontHeight(fontHeight);
        font.setUnderline(underline);
        //CellStyle에 지정한 폰트 Set
        hssfCellStyle.setFont(font);

        //생성필요없이 바로 스타일 적용처리. 생성하면 이상하게 제목이 날라갑니다.
        //row 5, col 80까지 생성
        for(int rowIndex = firstRow; rowIndex < lastRow; rowIndex++){
            Row row = sheet.getRow(rowIndex);
            for (int colIndex = firstCol; colIndex <= lastCol; colIndex++) {
                Cell cell = row.getCell(colIndex);
                cell.setCellStyle(hssfCellStyle);
            }
        }

    }

    /**
     * 연장집계표 데이타 스타일 처리
     */
    private void setDataExtensionCellStyle(HSSFSheet sumExtensionSheet, int firstRow, int lastRow, int firstCol, int lastCol){

        short height = (short) 300;
        short fontHeight = (short) 110;

        short hAlignCenter = CellStyle.ALIGN_CENTER;
        short vAlignCenter = CellStyle.VERTICAL_CENTER;
        boolean bold = false;
        byte underline = U_NONE;

        //CellStyle 객체생성
        HSSFCellStyle hssfCellStyle = createCellStyle();
        //Cell alignment 지정하기
        hssfCellStyle.setAlignment(hAlignCenter);
        hssfCellStyle.setVerticalAlignment(vAlignCenter);

        //Font 객체생성
        Font font = getFontDataType();
        font.setBold(bold);
        font.setFontHeight(fontHeight);
        font.setUnderline(underline);
        //CellStyle에 지정한 폰트 Set
        hssfCellStyle.setFont(font);

        hssfCellStyle.setBorderTop((short)1);
        hssfCellStyle.setBorderBottom((short)1);
        hssfCellStyle.setBorderLeft((short)1);
        hssfCellStyle.setBorderRight((short)1);

        //생성필요없이 바로 스타일 적용처리. 생성하면 이상하게 제목이 날라갑니다.
        //row 5, col 80까지 생성
        for(int rowIndex = firstRow; rowIndex < lastRow; rowIndex++){
            Row row = sumExtensionSheet.getRow(rowIndex);
            for (int colIndex = firstCol; colIndex <= lastCol; colIndex++) {
                try {
                    Cell cell = row.getCell(colIndex);
                    cell.setCellStyle(hssfCellStyle);
                }catch(Exception e){

                }
            }
        }
    }

    /**
     * 엑셀 생성 시작 > 현장 야장 조사
     * for문으로 detailList array 기반으로 "현장 야장 조사" 탭 생성 및 내용을 기입하자
     * @param fieldBasicBeanS       //기본 데이타
     * @param fieldDetailList  //상세 리스트
     */
    public void createManholeExcel(FieldBasicBeanS fieldBasicBeanS, ArrayList<FieldDetailBeanS> fieldDetailList) {
        GomsLog.d(LOG_TAG, "createManholeExcel()");

        //CellStyle을 미리 만들어서 적용하즈아

        short hAlignCenter = CellStyle.ALIGN_CENTER;
        short hAlignLeft = CellStyle.ALIGN_LEFT;
        short vAlignCenter = CellStyle.VERTICAL_CENTER;
        short vAlignTop = CellStyle.VERTICAL_TOP;

        Font fontReportBg = mWorkbook.createFont();
        fontReportBg.setBold(false);
        fontReportBg.setFontHeight(TITLE_SIZE.TITLE.size);
        fontReportBg.setUnderline(U_NONE);

        Font fontReportValue = mWorkbook.createFont();
        fontReportValue.setBold(false);
        fontReportValue.setFontHeight(TITLE_SIZE.TITLE.size);
        fontReportValue.setUnderline(U_NONE);

        Font fontReportMainTitle = mWorkbook.createFont();
        fontReportMainTitle.setBold(true);
        fontReportMainTitle.setFontHeight(TITLE_SIZE.MAIN.size);
        fontReportMainTitle.setUnderline(U_DOUBLE);


        //CellStyle 객체생성 - 배경
        HSSFCellStyle hssfCellStyleBg = createCellStyle();
        hssfCellStyleBg.setAlignment(hAlignCenter);
        hssfCellStyleBg.setVerticalAlignment(vAlignCenter);
        hssfCellStyleBg.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hssfCellStyleBg.setFillPattern((short) 1);
        hssfCellStyleBg.setBorderTop((short)1);
        hssfCellStyleBg.setBorderBottom((short)1);
        hssfCellStyleBg.setBorderLeft((short)1);
        hssfCellStyleBg.setBorderRight((short)1);
        hssfCellStyleBg.setFont(fontReportBg);

        //CellStyle 객체생성 - 값
        HSSFCellStyle hssfCellStyleValue = createCellStyle();
        hssfCellStyleValue.setAlignment(hAlignCenter);
        hssfCellStyleValue.setVerticalAlignment(vAlignCenter);
        hssfCellStyleValue.setBorderTop((short)1);
        hssfCellStyleValue.setBorderBottom((short)1);
        hssfCellStyleValue.setBorderLeft((short)1);
        hssfCellStyleValue.setBorderRight((short)1);
        hssfCellStyleValue.setFont(fontReportValue);

        //CellStyle 객체생성 - 특이사항
        HSSFCellStyle hssfCellStyleRemark = createCellStyle();
        hssfCellStyleRemark.setAlignment(hAlignLeft);
        hssfCellStyleRemark.setVerticalAlignment(vAlignTop);
        hssfCellStyleRemark.setBorderTop((short)1);
        hssfCellStyleRemark.setBorderBottom((short)1);
        hssfCellStyleRemark.setBorderLeft((short)1);
        hssfCellStyleRemark.setBorderRight((short)1);
        hssfCellStyleRemark.setFont(fontReportValue);

        //CellStyle 객체생성 - 메인타이틀
        HSSFCellStyle hssfCellStyleMainTitle = createCellStyle();
        hssfCellStyleMainTitle.setAlignment(hAlignCenter);
        hssfCellStyleMainTitle.setVerticalAlignment(vAlignCenter);
        hssfCellStyleMainTitle.setFont(fontReportMainTitle);

        CreationHelper creationHelper = mWorkbook.getCreationHelper();

        //상세리스트별 현장야장조사 탭 생성 및 데이타 주입
        int page = 1;
        for(FieldDetailBeanS fieldDetailBeanS : fieldDetailList) {
            setManholeExcel(StringUtil.intToString(page), fieldBasicBeanS, fieldDetailBeanS, hssfCellStyleBg, hssfCellStyleValue, hssfCellStyleRemark, hssfCellStyleMainTitle, creationHelper);
            page++;
        }

        saveExcel(fieldBasicBeanS);

    }

    /**
     * 탭별로 현장조사야장 데이타 주입 처리
     * createManholeExcel에서 호출
     * @param fieldDetailBeanS
     */
    private void setManholeExcel(String page, FieldBasicBeanS fieldBasicBeanS, FieldDetailBeanS fieldDetailBeanS, HSSFCellStyle cellStyleBg, HSSFCellStyle cellStyleValue, HSSFCellStyle cellStyleRemark, HSSFCellStyle cellStyleMainTitle, CreationHelper creationHelper) {
        Log.d(LOG_TAG, "setManholeExcel() >>>> 페이지 : " + page + " 생성시작입니다.");

        HSSFSheet sheet = mWorkbook.createSheet(page);

        //첫번째 행 >> 현장 조사 야장
        Row row0 = sheet.createRow(0);
        setMainTitleStyle(sheet,  row0, CELL_STYLE.MAIN_TITLE, "현장 조사 야장", cellStyleMainTitle);

        //두번째 행
        Row row1 = sheet.createRow(1);
        setStyleDiv(sheet, row1,  CELL_STYLE.DIV_1, null);

        //세번째 행, 조사자
        Row row2 = sheet.createRow(2);

        setStyleDiv(sheet, row2,  CELL_STYLE.REPORTER_SPACE, "");
        setStyle(sheet, row2,  CELL_STYLE.REPORTER, "조사자:" + fieldBasicBeanS.getRes_mh_com_name(), cellStyleValue);

        //sheet.addMergedRegion(new CellRangeAddress(2, 2, 18, 19));

        //네번째 행
        Row row3 = sheet.createRow(3);

        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_NO, "NO.", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_LOCAL, "처리구역", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_DRAINAGE, "배제방식", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_NUM, "현장번호", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_STANDARD, "규격", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_DEPTH, "현장깊이(m)", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_LAT, "GPS위도", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_LNG, "GPS경도", cellStyleBg, creationHelper);
        setStyleBg(sheet, row3,  CELL_STYLE.HEAD_TITLE_MH_DATE, "조사일시", cellStyleBg, creationHelper);

        //각 항목의 value 대입
        Row row4 = sheet.createRow(4);
        //위경도처리
        String coordinate = fieldDetailBeanS.getRes_mh_coordinate();
        String lat = "";
        String lng = "";
        if (!StringUtil.isEmpty(coordinate)) {
            String[] latlng = coordinate.split(",");
            lat = latlng[0];
            lng = latlng[1];
        }
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_NO, page, cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_LOCAL, fieldBasicBeanS.getRes_mh_field_local(), cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_DRAINAGE, fieldDetailBeanS.getRes_mh_drainage(), cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_NUM, fieldDetailBeanS.getRes_mh_num(), cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_STANDARD, fieldDetailBeanS.getRes_mh_standard(), cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_DEPTH, fieldDetailBeanS.getRes_mh_depth(), cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_LAT, lat, cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_LNG, lng, cellStyleValue);
        setStyle(sheet, row4,  CELL_STYLE.VALUE_TITLE_MH_DATE, fieldDetailBeanS.getRes_mh_date(), cellStyleValue);

        Row row5 = sheet.createRow(5);
        setStyleBg(sheet, row5,  CELL_STYLE.HEAD_TITLE_MH_INSPECTION, "점검내용", cellStyleBg, creationHelper);
        setStyleBg(sheet, row5,  CELL_STYLE.HEAD_TITLE_MH_CAD, "도면", cellStyleBg, creationHelper);

        Row rowMhLid = sheet.createRow(CELL_STYLE.HEAD_TITLE_MH_LID.row);  //6

        setStyleBg(sheet, rowMhLid,  CELL_STYLE.HEAD_TITLE_MH_LID, "뚜껑", cellStyleBg, creationHelper);
        setStyleBg(sheet, rowMhLid,  CELL_STYLE.HEAD_TITLE_MH_LIB_DAMAGE, "파손", cellStyleBg, creationHelper);
        setStyle(sheet, rowMhLid,  CELL_STYLE.VALUE_TITLE_MH_LIB_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_lid_damage_lms()), cellStyleValue);
        setStyleBg(sheet, rowMhLid,  CELL_STYLE.HEAD_TITLE_MH_LIB_CRACK, "균열", cellStyleBg, creationHelper);
        setStyle(sheet, rowMhLid,  CELL_STYLE.VALUE_TITLE_MH_LIB_CRACK, convertData(fieldDetailBeanS.getRes_mh_lid_crack_lms()), cellStyleValue);
        setStyleBg(sheet, rowMhLid,  CELL_STYLE.HEAD_TITLE_MH_LIB_WATER, "유입수", cellStyleBg, creationHelper);
        setStyle(sheet, rowMhLid,  CELL_STYLE.VALUE_TITLE_MH_LIB_WATER, convertData(fieldDetailBeanS.getRes_mh_lid_water_lms()), cellStyleValue);
        setStyle(sheet, rowMhLid, CELL_STYLE.VALUE_TITLE_MH_CAD, "", cellStyleValue);  //CAD이미지 보여주기 >>> setPhoto에서 일괄처리함.

        Row rowMhOuter = sheet.createRow(CELL_STYLE.HEAD_TITLE_MH_OUTER.row);    //7

        setStyleBg(sheet, rowMhOuter,  CELL_STYLE.HEAD_TITLE_MH_OUTER, "현장주변부", cellStyleBg, creationHelper);
        setStyleBg(sheet, rowMhOuter,  CELL_STYLE.HEAD_TITLE_MH_OUTER_DAMAGE, "파손", cellStyleBg, creationHelper);
        setStyle(sheet, rowMhOuter,  CELL_STYLE.VALUE_TITLE_MH_OUTER_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_outer_damage_lms()), cellStyleValue);
        setStyleBg(sheet, rowMhOuter,  CELL_STYLE.HEAD_TITLE_MH_OUTER_CRACK, "균열", cellStyleBg, creationHelper);
        setStyle(sheet, rowMhOuter,  CELL_STYLE.VALUE_TITLE_MH_OUTER_CRACK, convertData(fieldDetailBeanS.getRes_mh_outer_crack_lms()), cellStyleValue);
        setStyleBg(sheet, rowMhOuter,  CELL_STYLE.HEAD_TITLE_MH_OUTER_WATER, "침입수등급(대중소)", cellStyleBg, creationHelper);
        setStyle(sheet, rowMhOuter,  CELL_STYLE.VALUE_TITLE_MH_OUTER_WATER, convertData(fieldDetailBeanS.getRes_mh_outer_water_lms()), cellStyleValue);

        Row row8 = sheet.createRow(8);

        setStyleBg(sheet, row8,  CELL_STYLE.HEAD_TITLE_MH_INNER, "현장내부", cellStyleBg, creationHelper);
        setStyleBg(sheet, row8,  CELL_STYLE.HEAD_TITLE_MH_INNER_DAMAGE, "파손", cellStyleBg, creationHelper);
        setStyle(sheet, row8,  CELL_STYLE.VALUE_TITLE_MH_INNER_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_inner_damage_lms()), cellStyleValue);
        setStyleBg(sheet, row8,  CELL_STYLE.HEAD_TITLE_MH_INNER_CRACK, "균열", cellStyleBg, creationHelper);
        setStyle(sheet, row8,  CELL_STYLE.VALUE_TITLE_MH_INNER_CRACK, convertData(fieldDetailBeanS.getRes_mh_inner_crack_lms()), cellStyleValue);
        setStyle(sheet, row8,  CELL_STYLE.VALUE_TITLE_MH_INNER_WATER, convertData(fieldDetailBeanS.getRes_mh_inner_water_lms()), cellStyleValue);

        Row row9 = sheet.createRow(9);

        setStyleBg(sheet, row9,  CELL_STYLE.HEAD_TITLE_MH_PIPE, "관로접합부", cellStyleBg, creationHelper);
        setStyleBg(sheet, row9,  CELL_STYLE.HEAD_TITLE_MH_PIPE_DAMAGE, "파손", cellStyleBg, creationHelper);
        setStyle(sheet, row9,  CELL_STYLE.VALUE_TITLE_MH_PIPE_DAMAGE, convertData(fieldDetailBeanS.getRes_mh_pipe_damage_lms()), cellStyleValue);
        setStyleBg(sheet, row9,  CELL_STYLE.HEAD_TITLE_MH_PIPE_CRACK, "균열", cellStyleBg, creationHelper);
        setStyle(sheet, row9,  CELL_STYLE.VALUE_TITLE_MH_PIPE_CRACK, convertData(fieldDetailBeanS.getRes_mh_pipe_crack_lms()), cellStyleValue);
        setStyle(sheet, row9,  CELL_STYLE.VALUE_TITLE_MH_PIPE_WATER, convertData(fieldDetailBeanS.getRes_mh_pipe_water_lms()), cellStyleValue);

        Row rowLadder = sheet.createRow(10);

        setStyleBg(sheet, rowLadder,  CELL_STYLE.HEAD_TITLE_MH_LADDER, "사다리", cellStyleBg, creationHelper);
        setStyleBg(sheet, rowLadder,  CELL_STYLE.HEAD_TITLE_MH_LADDER_YN, "유/무", cellStyleBg, creationHelper);
        setStyle(sheet, rowLadder,  CELL_STYLE.VALUE_TITLE_MH_LADDER_YN, convertData(fieldDetailBeanS.getRes_mh_ladder_yn()), cellStyleValue);
        setStyleBg(sheet, rowLadder,  CELL_STYLE.HEAD_TITLE_MH_LADDER_DAMAGE, "사다리손상", cellStyleBg, creationHelper);
        setStyleBg(sheet, rowLadder,  CELL_STYLE.HEAD_TITLE_MH_LADDER_DAMAGE_LMS, "대중소", cellStyleBg, creationHelper);
        setStyle(sheet, rowLadder,  CELL_STYLE.VALUE_TITLE_MH_LADDER_DAMAGE_LMS, convertData(fieldDetailBeanS.getRes_mh_ladder_damage_lms()), cellStyleValue);

        Row row11 = sheet.createRow(11);

        setStyleBg(sheet, row11,  CELL_STYLE.HEAD_TITLE_MH_INVERT, "인버트", cellStyleBg, creationHelper);
        setStyle(sheet, row11,  CELL_STYLE.VALUE_TITLE_MH_INVERT_YN, convertData(fieldDetailBeanS.getRes_mh_invert_yn()), cellStyleValue);
        setStyleBg(sheet, row11,  CELL_STYLE.HEAD_TITLE_MH_ENDOTHELIUM, "내피생성", cellStyleBg, creationHelper);
        setStyle(sheet, row11,  CELL_STYLE.VALUE_TITLE_MH_ENDOTHELIUM_LMS, convertData(fieldDetailBeanS.getRes_mh_endothelium_lms()), cellStyleValue);

        Row rowOdor = sheet.createRow(CELL_STYLE.HEAD_TITLE_MH_ODOR.row);

        setStyleBg(sheet, rowOdor,  CELL_STYLE.HEAD_TITLE_MH_ODOR, "악취발생", cellStyleBg, creationHelper);
        setStyleBg(sheet, rowOdor,  CELL_STYLE.HEAD_TITLE_MH_ODOR_LMS, "양호,대중소", cellStyleBg, creationHelper);
        setStyle(sheet, rowOdor,  CELL_STYLE.VALUE_TITLE_MH_ODOR_YN, convertData(fieldDetailBeanS.getRes_mh_odor_glms()), cellStyleValue);
        setStyleBg(sheet, rowOdor,  CELL_STYLE.HEAD_TITLE_MH_WASTEOIL_LMS, "폐유부착", cellStyleBg, creationHelper);
        setStyle(sheet, rowOdor,  CELL_STYLE.VALUE_TITLE_MH_WASTEOIL_LMS, convertData(fieldDetailBeanS.getRes_mh_wasteoil_lms()), cellStyleValue);

        Row rowLidSealing = sheet.createRow(CELL_STYLE.HEAD_TITLE_MH_LID_SEALING.row);

        setStyleBg(sheet, rowLidSealing,  CELL_STYLE.HEAD_TITLE_MH_LID_SEALING, "뚜껑밀폐\n(개폐불가)", cellStyleBg, creationHelper);
        setStyleBg(sheet, rowLidSealing,  CELL_STYLE.HEAD_TITLE_MH_LID_SEALING_YN, "여/부", cellStyleBg, creationHelper);
        setStyle(sheet, rowLidSealing,  CELL_STYLE.VALUE_TITLE_MH_LID_SEALING_YN, convertData(fieldDetailBeanS.getRes_mh_lid_sealing_yn()), cellStyleValue);
        setStyleBg(sheet, rowLidSealing,  CELL_STYLE.HEAD_TITLE_MH_TEMP_OBSTACLE_LMS, "임시장애물", cellStyleBg, creationHelper);
        setStyle(sheet, rowLidSealing,  CELL_STYLE.VALUE_TITLE_MH_TEMP_OBSTACLE_LMS, convertData(fieldDetailBeanS.getRes_mh_temp_obstacle_glms()), cellStyleValue);

        Row rowRootIntrusion = sheet.createRow(14);

        setStyleBg(sheet, rowRootIntrusion,  CELL_STYLE.HEAD_TITLE_MH_ROOT_INTRUSION_LMS, "뿌리 침입", cellStyleBg, creationHelper);
        setStyle(sheet, rowRootIntrusion,  CELL_STYLE.VALUE_TITLE_MH_ROOT_INTRUSION_LMS, convertData(fieldDetailBeanS.getRes_mh_root_intrusion_glms()), cellStyleValue);

        if ("Y".equals(MyApplication.getInstance().prefs().get(ManHolePrefs.MH_BLOCK_IMPORT_YN))) {
            //블록이음부
            Row row15 = sheet.createRow(15);
            setStyleBg(sheet, row15,  CELL_STYLE.HEAD_TITLE_MH_BLOCK_GAP, "블록이음부단차", cellStyleBg, creationHelper);
            setStyleBg(sheet, row15,  CELL_STYLE.HEAD_TITLE_MH_BLOCK_GAP_LMS, "대중소", cellStyleBg, creationHelper);
            setStyle(sheet, row15,  CELL_STYLE.VALUE_TITLE_MH_BLOCK_GAP_LMS, convertData(fieldDetailBeanS.getRes_mh_block_gap_lms()), cellStyleValue);
            setStyle(sheet, row15,  CELL_STYLE.SPACE_TITLE_MH_BLOCK_01, "", cellStyleValue);

            Row row16 = sheet.createRow(16);

            setStyleBg(sheet, row16,  CELL_STYLE.HEAD_TITLE_MH_BLOCK_LEAVE, "블록이음부이탈", cellStyleBg, creationHelper);
            setStyle(sheet, row16,  CELL_STYLE.VALUE_TITLE_MH_BLOCK_LEAVE_LMS, convertData(fieldDetailBeanS.getRes_mh_block_leave_lms()), cellStyleValue);

            Row row17 = sheet.createRow(17);

            setStyleBg(sheet, row17,  CELL_STYLE.HEAD_TITLE_MH_BLOCK_DEMEGE, "블록이음부손상", cellStyleBg, creationHelper);
            setStyle(sheet, row17,  CELL_STYLE.VALUE_TITLE_MH_BLOCK_DEMEGE_LMS, convertData(fieldDetailBeanS.getRes_mh_block_damage_lms()), cellStyleValue);

            Row row18 = sheet.createRow(18);

            setStyleBg(sheet, row18,  CELL_STYLE.HEAD_TITLE_MH_SURFACE_GAP, "표면단차", cellStyleBg, creationHelper);
            setStyle(sheet, row18,  CELL_STYLE.VALUE_TITLE_MH_SURFACE_GAP_LMS, convertData(fieldDetailBeanS.getRes_mh_surface_gap_lms()), cellStyleValue);

            Row rowDivPhoto = sheet.createRow(CELL_STYLE.DIV_PHOTO.row);

            setStyle(sheet, rowDivPhoto,  CELL_STYLE.DIV_PHOTO, "", cellStyleValue);

            //사진대지
            Row rowTitlePhoto = sheet.createRow(CELL_STYLE.HEAD_TITLE_PHOTO.row);

            setStyleBg(sheet, rowTitlePhoto,  CELL_STYLE.HEAD_TITLE_PHOTO, "사진대지", cellStyleBg, creationHelper);

            Row rowPhoto1 = sheet.createRow(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG.row);
            Row rowPhotoTitle1 = sheet.createRow(CELL_STYLE.HEAD_TITLE_PHOTO_AROUND.row);
            Row rowPhoto2 = sheet.createRow(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG.row);
            Row rowPhotoTitle2 = sheet.createRow(CELL_STYLE.HEAD_TITLE_PHOTO_INNER.row);

            setStyle(sheet, rowPhoto1,  CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG, "", cellStyleValue);
            setStyle(sheet, rowPhoto1,  CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG, "", cellStyleValue);

            setStyleBg(sheet, rowPhotoTitle1,  CELL_STYLE.HEAD_TITLE_PHOTO_AROUND, "현장전경", cellStyleBg, creationHelper);
            setStyleBg(sheet, rowPhotoTitle1,  CELL_STYLE.HEAD_TITLE_PHOTO_OUTER, "현장외부", cellStyleBg, creationHelper);

            setStyle(sheet, rowPhoto2,  CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG, "", cellStyleValue);
            setStyle(sheet, rowPhoto2,  CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG, "", cellStyleValue);

            setStyleBg(sheet, rowPhotoTitle2,  CELL_STYLE.HEAD_TITLE_PHOTO_INNER, "현장내부", cellStyleBg, creationHelper);
            setStyleBg(sheet, rowPhotoTitle2,  CELL_STYLE.HEAD_TITLE_PHOTO_ETC, "특이사항", cellStyleBg, creationHelper);

            //특이사항
            Row divEtc = sheet.createRow(CELL_STYLE.DIV_ETC.row);
            setStyle(sheet, divEtc,  CELL_STYLE.DIV_ETC, "", cellStyleValue);

            //특이사항
            Row rowEtcTitle = sheet.createRow(CELL_STYLE.HEAD_TITLE_ETC.row);

            setStyleBg(sheet, rowEtcTitle,  CELL_STYLE.HEAD_TITLE_ETC, "특이사항", cellStyleBg, creationHelper);
            setStyleBg(sheet, rowEtcTitle,  CELL_STYLE.HEAD_TITLE_MH_LID_SIZE, "뚜껑크기", cellStyleBg, creationHelper);
            setStyle(sheet, rowEtcTitle,  CELL_STYLE.VALUE_TITLE_MH_LID_SIZE, fieldDetailBeanS.getRes_mh_size(), cellStyleValue);
            setStyleBg(sheet, rowEtcTitle,  CELL_STYLE.HEAD_TITLE_MH_MATERIAL, "뚜껑재질", cellStyleBg, creationHelper);
            setStyle(sheet, rowEtcTitle,  CELL_STYLE.VALUE_TITLE_MH_MATERIAL, fieldDetailBeanS.getRes_mh_material(), cellStyleValue);

            Row row41 = sheet.createRow(CELL_STYLE.VALUE_TITLE_ETC.row);
            setStyleRemark(sheet, row41,  CELL_STYLE.VALUE_TITLE_ETC, fieldDetailBeanS.getRes_mh_remark(), cellStyleRemark);

            //블록시, 우측에 라인 생성하기
            HSSFCellStyle hssfCellStyleRight = createCellStyle();
            hssfCellStyleRight.setBorderRight((short)1);
            for(int i = CELL_STYLE.SPACE_TITLE_MH_BLOCK_01.firstRow; i <= CELL_STYLE.SPACE_TITLE_MH_BLOCK_01.lastRow; i++){
                if(i == CELL_STYLE.SPACE_TITLE_MH_BLOCK_01.firstRow){
                    hssfCellStyleRight.setBorderTop((short)1);
                }
                Row row = sheet.getRow(i);
                if(row == null){
                    row = sheet.createRow(i);
                }
                Cell cell = row.getCell(CELL_STYLE.SPACE_TITLE_MH_BLOCK_01.lastCol);
                if(cell == null){
                    cell = row.createCell(CELL_STYLE.SPACE_TITLE_MH_BLOCK_01.lastCol);
                }
                cell.setCellStyle(hssfCellStyleRight);
            }
        }else{
            Row rowDivPhoto = sheet.createRow(CELL_STYLE.DIV_PHOTO_ELSE.row);

            setStyle(sheet, rowDivPhoto,  CELL_STYLE.DIV_PHOTO_ELSE, "", cellStyleValue);

            //사진대지
            Row rowTitlePhoto = sheet.createRow(CELL_STYLE.HEAD_TITLE_PHOTO_ELSE.row);

            setStyleBg(sheet, rowTitlePhoto,  CELL_STYLE.HEAD_TITLE_PHOTO_ELSE, "사진대지", cellStyleBg, creationHelper);

            Row rowPhoto1 = sheet.createRow(CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG_ELSE.row);
            Row rowPhotoTitle1 = sheet.createRow(CELL_STYLE.HEAD_TITLE_PHOTO_AROUND_ELSE.row);
            Row rowPhoto2 = sheet.createRow(CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG_ELSE.row);
            Row rowPhotoTitle2 = sheet.createRow(CELL_STYLE.HEAD_TITLE_PHOTO_INNER_ELSE.row);

            setStyle(sheet, rowPhoto1,  CELL_STYLE.VALUE_TITLE_PHOTO_AROUND_IMG_ELSE, "", cellStyleValue);
            setStyle(sheet, rowPhoto1,  CELL_STYLE.VALUE_TITLE_PHOTO_OUTER_IMG_ELSE, "", cellStyleValue);

            setStyleBg(sheet, rowPhotoTitle1,  CELL_STYLE.HEAD_TITLE_PHOTO_AROUND_ELSE, "현장전경", cellStyleBg, creationHelper);
            setStyleBg(sheet, rowPhotoTitle1,  CELL_STYLE.HEAD_TITLE_PHOTO_OUTER_ELSE, "현장외부", cellStyleBg, creationHelper);

            setStyle(sheet, rowPhoto2,  CELL_STYLE.VALUE_TITLE_PHOTO_INNER_IMG_ELSE, "", cellStyleValue);
            setStyle(sheet, rowPhoto2,  CELL_STYLE.VALUE_TITLE_PHOTO_ETC_IMG_ELSE, "", cellStyleValue);

            setStyleBg(sheet, rowPhotoTitle2,  CELL_STYLE.HEAD_TITLE_PHOTO_INNER_ELSE, "현장내부", cellStyleBg, creationHelper);
            setStyleBg(sheet, rowPhotoTitle2,  CELL_STYLE.HEAD_TITLE_PHOTO_ETC_ELSE, "특이사항", cellStyleBg, creationHelper);

            //특이사항
            Row divEtc = sheet.createRow(CELL_STYLE.DIV_ETC_ELSE.row);
            setStyle(sheet, divEtc,  CELL_STYLE.DIV_ETC_ELSE, "", cellStyleValue);

            //특이사항
            Row rowEtcTitle = sheet.createRow(CELL_STYLE.HEAD_TITLE_ETC_ELSE.row);

            setStyleBg(sheet, rowEtcTitle,  CELL_STYLE.HEAD_TITLE_ETC_ELSE, "특이사항", cellStyleBg, creationHelper);
            setStyleBg(sheet, rowEtcTitle,  CELL_STYLE.HEAD_TITLE_MH_LID_SIZE_ELSE, "뚜껑크기", cellStyleBg, creationHelper);
            setStyle(sheet, rowEtcTitle,  CELL_STYLE.VALUE_TITLE_MH_LID_SIZE_ELSE, fieldDetailBeanS.getRes_mh_size(), cellStyleValue);
            setStyleBg(sheet, rowEtcTitle,  CELL_STYLE.HEAD_TITLE_MH_MATERIAL_ELSE, "뚜껑재질", cellStyleBg, creationHelper);
            setStyle(sheet, rowEtcTitle,  CELL_STYLE.VALUE_TITLE_MH_MATERIAL_ELSE, fieldDetailBeanS.getRes_mh_material(), cellStyleValue);

            Row row41 = sheet.createRow(CELL_STYLE.VALUE_TITLE_ETC_ELSE.row);
            setStyleRemark(sheet, row41,  CELL_STYLE.VALUE_TITLE_ETC_ELSE, fieldDetailBeanS.getRes_mh_remark(), cellStyleRemark);
        }

        HSSFCellStyle hssfCellStyleLeft = createCellStyle();
        hssfCellStyleLeft.setBorderLeft((short)1);

        Cell iCell0, iCell3, iCell4;
        try {
            iCell0 = rowRootIntrusion.getCell(0);
            iCell3 = rowRootIntrusion.getCell(3);
            iCell4 = rowRootIntrusion.getCell(4);
            if (iCell0 == null) {
                iCell0 = rowRootIntrusion.createCell(0);
                iCell3 = rowRootIntrusion.createCell(3);
                iCell4 = rowRootIntrusion.createCell(4);
            }
            iCell0.setCellStyle(hssfCellStyleLeft);
            iCell3.setCellStyle(hssfCellStyleLeft);
            iCell4.setCellStyle(hssfCellStyleLeft);
        } catch (NullPointerException e) {

        }

        //도면 우측 라인 생성하기
        HSSFCellStyle hssfCellStyleRight = createCellStyle();
        hssfCellStyleRight.setBorderRight((short)1);
        for(int i = CELL_STYLE.VALUE_TITLE_MH_CAD.firstRow; i <= CELL_STYLE.VALUE_TITLE_MH_CAD.lastRow; i++){
            Row row = sheet.getRow(i);
            if(row == null){
                row = sheet.createRow(i);
            }
            Cell cell = row.getCell(CELL_STYLE.VALUE_TITLE_MH_CAD.lastCol);
            if(cell == null){
                cell = row.createCell(CELL_STYLE.VALUE_TITLE_MH_CAD.lastCol);
            }

            cell.setCellStyle(hssfCellStyleRight);
        }

        setPaperAligment(sheet);
        setColumeWidth(sheet, SHEET_TYPE.DETAIL);

        try {
            setServerPhoto(sheet, fieldDetailBeanS);
        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
        }
    }


    private void saveExcel(FieldBasicBeanS fieldBasicBeanS){
        FileOutputStream fileOut = null;

        //File file = FileUtil.createFile(mActivity, Environment.DIRECTORY_DOCUMENTS, "excel", ".xls");
        String prefix = "";
        if(!StringUtil.isEmpty(fieldBasicBeanS.getRes_mh_field_title())){
            prefix = fieldBasicBeanS.getRes_mh_field_title();
            prefix = prefix.replace(" ", "").trim();
        }else{
            prefix = fieldBasicBeanS.getRes_mh_field_idx();
        }

        File file = FileUtil.createExcelFile(mActivity, prefix, Environment.DIRECTORY_DOCUMENTS, "excel", ".xls");

        try {
            fileOut = new FileOutputStream(file);
            mWorkbook.write(fileOut);
        }catch (IOException e){
            GomsLog.d(LOG_TAG, e.toString());
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                    mWorkbook.close();

                    MediaScannerConnection.scanFile(mActivity,
                            new String[]{file.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    // Handle scan completion, if needed
                                    GomsLog.d(LOG_TAG, "path : " + path);
                                    mExcelInterface.onComplete();
                                }
                            });

                } catch (IOException e) {
                    GomsLog.d(LOG_TAG, e.toString());
                }
            }
        }
    }

    private String convertData(String originalData){
        if("Y".equalsIgnoreCase(originalData)){
            return "O";
        }else if("N".equalsIgnoreCase(originalData)){
            return "X";
        }else if("G".equalsIgnoreCase(originalData)){
            return "양호";
        }else if("L".equalsIgnoreCase(originalData)){
            return "대";
        }else if("M".equalsIgnoreCase(originalData)){
            return "중";
        }else if("S".equalsIgnoreCase(originalData)){
            return "소";
        }
        return originalData;
    }

    ExcelInterface mExcelInterface;

    public ExcelInterface getExcelInterface() {
        return mExcelInterface;
    }

    public void setExcelInterface(ExcelInterface mExcelInterface) {
        this.mExcelInterface = mExcelInterface;
    }

    public interface ExcelInterface{
        void onComplete();
    }
}
