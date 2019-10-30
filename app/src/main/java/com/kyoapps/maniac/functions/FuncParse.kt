package com.kyoapps.maniac.functions

import com.kyoapps.maniac.room.entities.ThreadEnt
import okhttp3.ResponseBody
import retrofit2.Response
import com.kyoapps.maniac.helpers.C_COMMON
import com.kyoapps.maniac.helpers.classes.pojo.Board
import com.kyoapps.maniac.room.entities.ReplyEnt
import android.text.Html


//todo replace with new api
object FuncParse {

    // bad code. use new api asap
    fun parseThreadsLegacy(response: Response<ResponseBody>, brdid: Short): List<ThreadEnt> {

        val timeMs = System.currentTimeMillis()

        val resultList: ArrayList<ThreadEnt> = ArrayList()

        var reply = ""
        var cleanID: String
        var cleanIDTemp = ""
        var temp2 = ""
        var ok = false
        var gotTitle = false
        var end: Int
        var middle: Int
        var repliesNrStart: Int
        var index = 0

        response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine {
            if (!ok and it.startsWith("<p style=\"font")) ok = true

            if (ok) {
                middle = it.indexOf("\"><fo")
                end = it.indexOf("/a> - <")

                repliesNrStart = it.indexOf("Antworten: ")
                if (repliesNrStart > 0) {
                    val tempReply = it.substring(repliesNrStart)
                    val tempReplyStop = tempReply.indexOf("</")
                    reply = if (tempReplyStop > 0)
                        tempReply.substring(11, tempReplyStop)
                    else
                        tempReply.substring(11, tempReply.indexOf(" )"))
                }

                if (middle > 0) {
                    val titleID = it.substring(middle - 9, middle - 3)
                    cleanID = titleID.replace("(", "")
                    cleanIDTemp = cleanID

                    if (end > 0) {
                        val title = it.substring(middle + 17, end - 8)
                        val temp1 = title.replace("&amp;", "&")
                        temp2 = temp1.replace("&quot;", "\"").replace("&gt;", ">")
                        gotTitle = true

                    } else {
                        val title = it.substring(middle + 17)
                        val temp1 = title.replace("&amp;", "&")
                        temp2 = temp1.replace("&quot;", "\"").replace("&gt;", ">")
                        //gotTitle = true
                    }
                } else {
                    if (end > 0) {
                        val title = it.substring(0, end - 1)
                        val temp1 = title.replace("&amp;", "&")
                        temp2 += temp1.replace("&quot;", "\"").replace("&gt;", ">")
                        gotTitle = true
                    }
                }

                if (repliesNrStart > 0 && gotTitle) {
                    //Log.d(TAG, "time ${timeMs + index} title: $temp2")
                    resultList.add(ThreadEnt(brdid, cleanIDTemp.toInt(), temp2, reply.toInt(), -1, false, timeMs + index))
                    index ++ //add index to time to sort later
                    gotTitle = false
                }
            }

        }
        return resultList
    }

    // bad code. use new api asap
    fun parseRepliesLegacy(response: Response<ResponseBody>, brdid: Short, thrdid: Int): List<ReplyEnt> {
        val timeMs = System.currentTimeMillis()

        val resultList: ArrayList<ReplyEnt> = ArrayList()

        var mUser = ""
        var cleanIDtemp = ""
        val mSpace = StringBuilder()
        var mSpaceTemp = ""
        var titleTemp = ""
        var timestamp: String
        var ok = true
        var index = 0
        var addSpace = 0

        response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine {

            if (it == "<ul>") addSpace++

            val uLClose = it.indexOf("<li type=")
            if (uLClose > 0) { addSpace -= (it.length - it.replace("</ul>", "</ul").length) }

            val middle = it.indexOf("><font")
            if (middle > 0) {
                ok = true

                var end = it.indexOf("</font><")
                if (end == -1) end = it.length
                titleTemp = it.substring(middle + 16, end).replace("&amp;", "&").replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">")
                val titleID = it.substring(middle - 8, middle - 1)
                val cleanID = titleID.replace("p", "")

                if (addSpace > 0) { for (i in 1 until addSpace)  mSpace.append("   ") }

                mSpaceTemp = mSpace.toString()
                cleanIDtemp = cleanID
            }

            if (addSpace > -1 && ok && !it.contains("<")) {
                mUser = it

                mSpace.setLength(0)
                ok = false
            }

            if (it.startsWith("</b> -")) {
                timestamp = it.substring(7, 21)
                resultList.add(ReplyEnt(brdid, thrdid, cleanIDtemp.toInt(), "$mSpaceTemp$titleTemp", "$mSpaceTemp$mUser", timestamp, false, timeMs + index))
                index ++ //add index to time to sort later
            }
        }
        return resultList
    }

    // bad code. use new api asap
    fun parseMessageLegacy(response: Response<ResponseBody>, showImg: Boolean, showGif: Boolean): String {

        var readLine = false
        var isQuote = false
        val messageBuilder = StringBuilder()

        var line: String
        response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine { string ->
            line = string

            if (line.trim { it <= ' ' } == "<tr class=\"bg2\">") {
                readLine = true
            }
            if (readLine) {
                if (line.contains("images/spoiler.png")) {
                    //Log.d("SPOILER_LINE BEFORE", line.substring(50))
                    //line = line.replace("images/spoiler.png", "file:///android_asset/images/spoiler_2.png").replace("width=\"17\" height=\"15\"", "");
                    line = line.replace("images/spoiler.png", C_COMMON.MANIAC_BASE_URL + "/images/spoiler.png")
                    //Log.d("SPOILER_LINE AFTER", line.substring(50))
                }

                if (!isQuote) {
                    if (line.contains("<font color=\"808080\">")) {
                        isQuote = true
                    }
                }

                if (showImg && !isQuote && line.contains("http")) {
                    if (showGif) {
                        //Log.d("LINE IMG", line)
                        val image = line.indexOf(".jpg\" ta") + line.indexOf(".png\" ta") + line.indexOf(".gif\" ta")
                        //Log.d(Integer.toString(line.indexOf(".jpg\" ta")) + " " + Integer.toString(line.indexOf(".png\" ta")), "VALUES");
                        if (image > 0) {
                            val lineEnd = line.indexOf("</a>]", image) + 5
                            if (line.indexOf("</font>") > 0) {
                                line = line.replace("[<a href", "<div class=\"img-wraper\"><img src").substring(0, image + 31) + "/></div></font>" + line.substring(lineEnd)
                            } else
                                line = line.replace("[<a href", "<div class=\"img-wraper\"><img src").substring(0, image + 31) + "/></div>" + line.substring(lineEnd)
                            if (line.contains("www.dropbox.com"))
                                line = line.replace("www.dropbox.com", "dl.dropboxusercontent.com")
                            //Log.d("LINE WITH IMAGE", line)
                        }
                    } else {
                        val image = line.indexOf(".jpg\" ta") + line.indexOf(".png\" ta")
                        //Log.d(Integer.toString(line.indexOf(".jpg\" ta")) + " " + Integer.toString(line.indexOf(".png\" ta")), "VALUES");
                        if (image > 0) {
                            val lineEnd = line.indexOf("</a>]", image) + 5
                            //Log.d(line, "IMAGE BEFORE");
                            line = if (line.indexOf("</font>") > 0) {
                                line.replace("[<a href", "<div class=\"img-wraper\"><img src").substring(0, image + 30) + "/></div></font>" + line.substring(lineEnd)
                            } else
                                line.replace("[<a href", "<div class=\"img-wraper\"><img src").substring(0, image + 30) + "/></div>" + line.substring(lineEnd)
                            if (line.contains("www.dropbox.com"))
                                line = line.replace("www.dropbox.com", "dl.dropboxusercontent.com")
                            //Log.d("IMAGE AFTER", line);
                            //Log.d(line.substring(line.length()-30, line.length()), "LINE WITH IMAGE");
                        }
                    }
                    var youtube = line.indexOf("youtu")
                    if (youtube != -1) {
                        val start: Int
                        youtube = line.indexOf("youtube.com/watch?")
                        if (youtube == -1) {
                            youtube = line.indexOf("youtu.be/")
                            if (youtube != -1) {
                                start = youtube + 9
                                line = line + "<br><div class=\"img-wraper\"><a href=\"http://www.youtube.com/watch?v=" + line.substring(start, start + 11) + "\">" +
                                            "<img src=\"http://img.youtube.com/vi/" + line.substring(start, start + 11) + "/hqdefault.jpg\" ></a></div><br>"

                            }
                        } else {

                            //Log.d(line.substring(60), "youtube line 0");
                            start = line.indexOf("v=", youtube) + 2

                            line = line + "<br><div class=\"img-wraper\"><a href=\"http://www.youtube.com/watch?v=" + line.substring(start, start + 11) + "\">" +
                                        "<img src=\"http://img.youtube.com/vi/" + line.substring(start, start + 11) + "/hqdefault.jpg\" ></a></div><br>"
                            //Log.d(line, "youtube line");
                        }
                    }

                }

                if (isQuote) {
                    if (line.contains("</font>")) {
                        isQuote = false
                    }
                }


                messageBuilder.append(line)
                ////Log.d(line, "LINE");

                if (line.trim { it <= ' ' } == "</tr>") {
                    readLine = false
                }

            }
        }
        return messageBuilder.toString()
    }

    // use new api
    fun parseBoardsLegacy(response: Response<ResponseBody>): List<Board> {
        val resultList = ArrayList<Board>()

        response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine {
            val start = it.indexOf("<a href=\"pxmboard.php?mode=board&brdid=")
            if (start != -1 && it.length > start + 39) {
                val subStringStart = it.substring(start + 39)
                val endId = subStringStart.indexOf("\">")
                if (endId != -1) {
                    val brdid = Integer.parseInt(subStringStart.substring(0, endId)).toShort()
                    val label = Html.fromHtml(subStringStart.substring(endId + 2, subStringStart.indexOf("</a>"))).toString()
                    resultList.add(Board(brdid, label))
                }
            }
        }
        return resultList
    }

    fun parseMessageForThrdid(response: Response<ResponseBody>): Int {
        var result = -1
        val startKey = "&thrdid="
        val endKey = "&msgid"

        response.body()?.byteStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine {
            if (it.contains(startKey)) {
                val start = it.indexOf(startKey)
                val end = it.indexOf(endKey, start)
                if (end > start) result = it.substring(start + startKey.length, end).toIntOrNull()?: -1
                return@forEachLine
            }
        }
        return result
    }



    // bad code. use new api asap
    fun formatHtmlLegacy(content: String, title: String?, textColor: Int, quoteColor: Int, linkColor: Int, lineHeight: String): String {

        val textColorHex = String.format("%06X", 0xFFFFFF and textColor)
        val linkColorHex = String.format("%06X", 0xFFFFFF and linkColor)
        val quoteColorHex = String.format("%06X", 0xFFFFFF and quoteColor)

        var cont = content

        cont = cont.replace("<br><font face=\"Courier New\">", "").replace("<font color=\"808080\">", "<font color=\"848484\">").replace("\t\t</font><br><br></td>", "</font></td>")


        val img = "img {max-width: 100%;  height: auto; display: block;\n" +
                "    margin-left: auto;\n" +
                "    margin-right: auto;}\n"
        val imgWraper = ".img-wraper{margin: 0px -8px;}\n"


        val mJustify = "style=\"text-align:justify\""


        return "<html><head><meta name=\"viewport\" cont=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0\">" +
                "<style type=\"text/css\">\n" +
                img +
                "body {margin: 0px;}\n" +
                ".parent {\n" +
                "    margin: 3px 8px ;\n" +
                "}" +
                imgWraper +
                "@font-face {\n" +
                "    font-family: roboto;\n" +
                "    src: url('file:///android_asset/fonts/Roboto-Regular.ttf');\n" +
                "}" +
                "div { font-family: roboto, Verdana, sans-serif;" + lineHeight + "}" +
                "a {word-wrap: break-word; -webkit-tap-highlight-color: rgba(0, 0, 0, 0);}" +

                "</style>" +
                "</head>" +
                "<body text =\"" + textColorHex + "\" link=\"" + linkColorHex + "\" vlink=\"" + linkColorHex + "\"" + mJustify + ">" +
                "<div class=\"parent\">" +
                "<p><b>" + title + "</b></p>" +
                cont.replace("<br><font face=\"Courier New\">", "<font color=\"$textColorHex\">").replace("<font color=\"808080\">", "<font color=\"$quoteColorHex\">").replace("\t\t</font><br><br></td>", "</font></td>") + //.replace("<br />", "") +

                "</div>" +
                "<script lang=\"javascript\" type=\"text/javascript\">\n" +
                "function spoiler(asResult) {\n" +
                "   if (asResult.nextSibling.style.display === 'none') {\n" +
                "       asResult.nextSibling.style.display = 'inline';\n" +
                "   }\n" +
                "   else {\n" +
                "       asResult.nextSibling.style.display = 'none';\n" +
                "   }\n" +
                "}" +
                "</script>" +
                "</body></html>"
    }

    const val TAG = "FuncParse"
}
