package de.saschat.rotmg.auth.util;

/***
 * Standard Body
 * 
 * game_net: string // "Unity"
 * play_platform: string // "Unity"
 * game_net_user_id: string?
 */
public class Constants {
    public static final String INFORMATION = "https://www.realmofthemadgod.com/app/init";
    public static final String INFORMATION_FORMAT = "https://www.realmofthemadgod.com/app/minit?platform=%s&key=%s";

    public static final String FILE_DIRECTORY_FORMAT = "https://rotmg-build.decagames.com/build/%s/%s/checksum.json";
    public static final String FILE_DOWNLOAD_FORMAT = "https://rotmg-build.decagames.com/build/%s/%s/%s";

    /***
     * POST
     */
    public static final String NEWS = "https://www.realmofthemadgod.com/unityNews/getNews";

    /***
     * POST
     *
     * guid: string // e-mail #1
     * password: string #2
     * clientToken: string #3
     * game_net: string #4
     */
    public static final String LOGIN = "https://www.realmofthemadgod.com/account/verify";
    public static final String LOGIN_STEAM = "https://www.realmofthemadgod.com/steamworks/getcredentials";
    public static final String FORGOT_PASSWORD = "https://www.realmofthemadgod.com/account/forgotPassword";

    /***
     * POST
     *
     * newGUID: string
     * newPassword: string
     * isAgeVerified: number
     * entrytag: string?
     * signedUpKabamEmail: number
     * name: string
     */
    public static final String REGISTER = "https://www.realmofthemadgod.com/account/register";

    /***
     * POST
     *
     * guid: string
     */
    public static final String RESEND_EMAIL = "https://www.realmofthemadgod.com/account/sendVerifyEmail";
}
