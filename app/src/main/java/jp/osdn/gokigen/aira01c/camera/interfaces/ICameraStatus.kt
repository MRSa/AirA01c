package jp.osdn.gokigen.aira01c.camera.interfaces

/**
 *
 */
interface ICameraStatus
{
    fun getStatusList(key: String): List<String?>
    fun getStatus(key: String): String
    fun getStatusColor(key: String): Int
    fun setStatus(key: String, value: String)

    companion object
    {
        const val TAKE_MODE = "exposureMode"     // プログラムモード(P/A/S/M)
        const val  SHUTTER_SPEED = "tv"           // シャッタースピード
        const val  APERTURE = "av"                // 絞り値
        const val  EXPREV = "xv"                  // 露出補正値
        const val  CAPTURE_MODE = "captureMode"   // キャプチャーモード
        const val  ISO_SENSITIVITY = "sv"         // ISO感度
        const val  WHITE_BALANCE = "WBMode"       // ホワイトバランス
        const val  AE = "meteringMode"            // 測光モード
        const val  EFFECT = "effect"              // ピクチャーエフェクトモード (フォトスタイルモード)
        const val  BATTERY = "battery"            // バッテリ残量
        const val  TORCH_MODE = "torch"           // 明かり（トーチ）の設定
        const val  FOCUS_STATUS = "focusStatus"   // フォーカス状態（合焦かどうか）
        const val  FOCAL_LENGTH = "focalLength"    // 焦点距離 (ズーム状態)
        const val  REMAIN_SHOTS = "remainShots"    // 残り撮影枚数

        const val  IMAGE_SIZE = "stillSize"        // 撮影画像サイズ
        const val  MOVIE_SIZE = "movieSize"        // 動画画像サイズ

        const val DRIVE_MODE = "shootMode"         // ドライブモード (単写・連写)

        //var STATE = "state"
        //var FOCUS_MODE = "focusMode"
        //var AF_MODE = "AFMode"

        //var RESOLUTION = "reso"

        //var AE_STATUS_MULTI = "multi"
        //var AE_STATUS_ESP = "ESP"
        //var AE_STATUS_SPOT = "spot"
        //var AE_STATUS_PINPOINT = "Spot"
        //var AE_STATUS_CENTER = "center"
        //var AE_STATUS_CENTER2 = "Ctr-Weighted"


        //var FLASH_XV = "flashxv"
        //var SELF_TIMER = "selftimer"

        //var TAKE_MODE_MOVIE = "movie"
    }
}
