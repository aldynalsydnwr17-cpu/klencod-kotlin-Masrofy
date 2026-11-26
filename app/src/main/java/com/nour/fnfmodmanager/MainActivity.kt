package com.nour.fnfmodmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

private val BG = Color(0xFF09070A)
private val PANEL = Color(0xFF171218)
private val RED = Color(0xFFF21D45)
private val MUTED = Color(0xFFAAA3AE)
private val GREEN = Color(0xFF19D66B)

data class ModItem(val name:String, val version:String, val gameVersion:String, val folder:String)

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("fnf_manager", MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary=RED, background=BG, surface=PANEL)) {
                ManagerApp(
                    getGameTree = { prefs.getString("game_tree", null)?.let(Uri::parse) },
                    saveGameTree = { uri -> prefs.edit().putString("game_tree", uri.toString()).apply() },
                    getGameVersion = { prefs.getString("game_version", "0.2.8") ?: "0.2.8" },
                    saveGameVersion = { prefs.edit().putString("game_version", it).apply() },
                    getMods = {
                        val a=JSONArray(prefs.getString("mods","[]"))
                        (0 until a.length()).map { i -> a.getJSONObject(i).let { ModItem(it.optString("name"),it.optString("version","1.0"),it.optString("gameVersion"),it.optString("folder")) } }
                    },
                    saveMods = { list -> prefs.edit().putString("mods",JSONArray().apply { list.forEach { put(JSONObject().put("name",it.name).put("version",it.version).put("gameVersion",it.gameVersion).put("folder",it.folder)) } }.toString()).apply() },
                    launchGame = {
                        val intent=packageManager.getLaunchIntentForPackage("com.ninjamuffin99.funkin")
                        if(intent!=null) startActivity(intent) else android.widget.Toast.makeText(this,"اللعبة غير مثبتة أو إصدارها مختلف",android.widget.Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ManagerApp(
    getGameTree:()->Uri?, saveGameTree:(Uri)->Unit,
    getGameVersion:()->String, saveGameVersion:(String)->Unit,
    getMods:()->List<ModItem>, saveMods:(List<ModItem>)->Unit, launchGame:()->Unit
) {
    var page by remember { mutableStateOf("الرئيسية") }
    var gameTree by remember { mutableStateOf(getGameTree()) }
    var gameVersion by remember { mutableStateOf(getGameVersion()) }
    var mods by remember { mutableStateOf(getMods()) }
    var status by remember { mutableStateOf("اختَر مجلد اللعبة لتفعيل التثبيت") }
    var showVersion by remember { mutableStateOf(false) }
    var selectedMod by remember { mutableStateOf<ModItem?>(null) }
    val context= androidx.compose.ui.platform.LocalContext.current

    val treePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if(uri!=null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            gameTree=uri; saveGameTree(uri); status="تم حفظ صلاحية مجلد اللعبة"
        }
    }
    val filePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) {
            val rootUri=gameTree
            if(rootUri==null) status="اختَر مجلد اللعبة أولًا"
            else {
                try {
                    val tree=DocumentFile.fromTreeUri(context,rootUri)!!
                    val modsDir=tree.findFile("Mods") ?: tree.createDirectory("Mods")
                    val fileName=DocumentFile.fromSingleUri(context,uri)?.name ?: "ImportedMod.zip"
                    val safe=fileName.substringBeforeLast('.',fileName).replace(Regex("[^A-Za-z0-9 _-]"),"").ifBlank{"Imported Mod"}
                    val dest=modsDir?.createDirectory(safe)
                    if(dest==null) throw Exception("تعذر إنشاء مجلد المود")
                    context.contentResolver.openInputStream(uri).use { input ->
                        if(fileName.endsWith(".zip",true)) {
                            ZipInputStream(BufferedInputStream(input)).use { zis ->
                                var entry=zis.nextEntry
                                while(entry!=null) {
                                    val clean=entry.name.replace("\\","/")
                                    if(!clean.startsWith("/") && !clean.split("/").any{it==".."} && !entry.isDirectory) {
                                        val parts=clean.split("/").filter{it.isNotBlank()}
                                        var parent=dest
                                        for(part in parts.dropLast(1)) parent=parent.findFile(part) ?: parent.createDirectory(part) ?: parent
                                        val out=parent.createFile("application/octet-stream",parts.last())
                                        if(out!=null) context.contentResolver.openOutputStream(out.uri).use { os -> if(os!=null) zis.copyTo(os) }
                                    }
                                    zis.closeEntry(); entry=zis.nextEntry
                                }
                            }
                        } else {
                            val out=dest.createFile("application/octet-stream",fileName)
                            if(out!=null) context.contentResolver.openOutputStream(out.uri).use { os -> if(os!=null) input.copyTo(os) }
                        }
                    }
                    val item=ModItem(safe,"1.0.0",gameVersion,safe)
                    mods=(mods.filterNot{it.folder==safe}+item); saveMods(mods)
                    status="تم استيراد المود إلى مجلد Mods"
                } catch(e:Exception) { status="فشل التثبيت: ${e.message ?: "تحقق من صلاحيات المجلد"}" }
            }
        }
    }

    Column(Modifier.fillMaxSize().background(BG)) {
        Row(Modifier.fillMaxWidth().padding(horizontal=18.dp, vertical=12.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={page="الإعدادات"}) { Icon(Icons.Default.Settings,null,tint=Color.White) }
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                Text("FNF",fontSize=31.sp,fontWeight=FontWeight.Black,color=Color.White)
                Text("MOD MANAGER",fontSize=12.sp,fontWeight=FontWeight.Bold,color=RED)
            }
            IconButton(onClick={status="لا توجد تنبيهات جديدة"}) { Icon(Icons.Default.Notifications,null,tint=Color.White) }
        }
        when(page) {
            "الرئيسية" -> LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=16.dp, vertical=8.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(125.dp).background(Brush.linearGradient(listOf(Color(0xFF35101D),BG,Color(0xFF230A17))),RoundedCornerShape(20.dp)).border(1.dp,RED,RoundedCornerShape(20.dp)).padding(15.dp)) {
                        Column(Modifier.align(Alignment.CenterEnd),horizontalAlignment=Alignment.End) {
                            Text("مدير مودات Friday Night Funkin’",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Color.White)
                            Text("استورد موداتك وثبّتها محليًا بسهولة",fontSize=12.sp,color=MUTED)
                            Spacer(Modifier.height(8.dp))
                            Text("تجربة آمنة • ملفاتك على جهازك",fontSize=12.sp,color=GREEN)
                        }
                    }
                }
                item {
                    Card(colors=CardDefaults.cardColors(containerColor=PANEL),shape=RoundedCornerShape(20.dp),border=BorderStroke(1.dp,RED)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                            Column(Modifier.weight(1f),horizontalAlignment=Alignment.End) {
                                Text("لعبة Friday Night Funkin’",fontSize=17.sp,fontWeight=FontWeight.Bold,color=Color.White)
                                Row(verticalAlignment=Alignment.CenterVertically) { Text("الإصدار: ",color=MUTED); Text(gameVersion,color=Color.White); Icon(Icons.Default.SportsEsports,null,tint=Color.White,modifier=Modifier.size(19.dp)) }
                                Text(gameTree?.lastPathSegment ?: "لم يتم اختيار المسار",fontSize=11.sp,color=MUTED,maxLines=1)
                                Text(if(gameTree!=null) "✓ تم اختيار مجلد اللعبة" else "لم يتم تحديد مجلد اللعبة",color=if(gameTree!=null) GREEN else Color(0xFFFFC34D),fontSize=12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick=launchGame,colors=ButtonDefaults.buttonColors(containerColor=RED),shape=CircleShape,modifier=Modifier.size(68.dp)) { Icon(Icons.Default.PlayArrow,null,tint=Color.White) }
                        }
                    }
                }
                item {
                    Column(Modifier.fillMaxWidth().background(PANEL,RoundedCornerShape(22.dp)).border(BorderStroke(2.dp,RED),RoundedCornerShape(22.dp)).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Text("إضافة مود جديد",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Color.White)
                        Text("اختر ملف المود من ملفات جهازك (ZIP)",fontSize=13.sp,color=MUTED)
                        Text("سيتم تثبيت المود تلقائيًا في مجلد Mods",fontSize=12.sp,color=MUTED)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick={ if(gameTree==null) treePicker.launch(null) else filePicker.launch(arrayOf("application/zip","application/octet-stream","*/*")) },modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=RED),shape=RoundedCornerShape(30.dp)) {
                            Icon(Icons.Default.FolderOpen,null); Spacer(Modifier.width(8.dp)); Text(if(gameTree==null) "اختيار مجلد اللعبة" else "اختيار مود من الملفات",fontWeight=FontWeight.Bold)
                        }
                        Text(status,fontSize=11.sp,color=GREEN,textAlign=TextAlign.Center,modifier=Modifier.padding(top=8.dp))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                        OutlinedButton(onClick={page="مكتبتي"}) { Text("عرض الكل",color=Color.White) }
                        Text("المودات المثبتة  ${mods.size}",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color.White)
                    }
                }
                items(mods.take(6)) { mod -> ModCard(mod,onDetails={selectedMod=mod;page="تفاصيل"},onPlay={status="اختَر مشغل اللعبة من الإعدادات أو شغّل FNF"}) }
            }
            "مكتبتي" -> {
                Column(Modifier.weight(1f).padding(16.dp)) {
                    Text("مكتبتي",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color.White)
                    Spacer(Modifier.height(10.dp))
                    if(mods.isEmpty()) Text("لم تستورد مودات بعد. استخدم صفحة الاستيراد.",color=MUTED)
                    LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) { items(mods) { m -> ModCard(m,{selectedMod=m;page="تفاصيل"},{status="لا يمكن تشغيل المود منفردًا؛ شغّل اللعبة أولًا"}) } }
                }
            }
            "استيراد مودات" -> Column(Modifier.weight(1f).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Text("استيراد مود",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Color.White)
                Spacer(Modifier.height(15.dp))
                OutlinedTextField(value=gameVersion,onValueChange={gameVersion=it;saveGameVersion(it)},label={Text("إصدار اللعبة")},singleLine=true)
                Spacer(Modifier.height(12.dp))
                Button(onClick={treePicker.launch(null)},colors=ButtonDefaults.buttonColors(containerColor=RED)) { Text("اختيار مجلد اللعبة") }
                Button(onClick={filePicker.launch(arrayOf("application/zip","*/*"))},colors=ButtonDefaults.buttonColors(containerColor=RED)) { Text("اختيار ملف ZIP") }
                Text(status,color=GREEN)
            }
            "الإعدادات" -> Column(Modifier.weight(1f).padding(18.dp),horizontalAlignment=Alignment.End) {
                Text("الإعدادات",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color.White)
                OutlinedTextField(value=gameVersion,onValueChange={gameVersion=it;saveGameVersion(it)},label={Text("إصدار FNF")},singleLine=true)
                Button(onClick={treePicker.launch(null)},colors=ButtonDefaults.buttonColors(containerColor=RED)) { Text("تغيير مجلد اللعبة") }
                Text("التطبيق لا يحمل مودات من الإنترنت. كل الملفات تختارها بنفسك.",color=MUTED,textAlign=TextAlign.End)
            }
            "تفاصيل" -> Column(Modifier.weight(1f).padding(18.dp),horizontalAlignment=Alignment.End) {
                val m=selectedMod
                Text(m?.name ?: "تفاصيل المود",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color.White)
                Text("إصدار المود: ${m?.version ?: "—"}",color=MUTED)
                Text("إصدار اللعبة: ${m?.gameVersion ?: "—"}",color=MUTED)
                Text("المجلد: ${m?.folder ?: "—"}",color=MUTED)
                Spacer(Modifier.height(18.dp))
                Button(onClick={
                    m?.let { mod ->
                        val root=gameTree?.let{DocumentFile.fromTreeUri(context,it)}
                        val modsDir=root?.findFile("Mods")
                        modsDir?.findFile(mod.folder)?.delete()
                        mods=mods.filterNot{it.folder==mod.folder};saveMods(mods);status="تمت إزالة المود"
                        page="مكتبتي"
                    }
                },colors=ButtonDefaults.buttonColors(containerColor=RED)) { Icon(Icons.Default.Delete,null); Text("إزالة المود بأمان") }
            }
        }
        NavigationBar(containerColor=Color(0xFF100B11),contentColor=Color.White) {
            listOf("الرئيسية","مكتبتي","استيراد مودات","الإعدادات").forEach { label ->
                NavigationBarItem(selected=page==label,onClick={page=label},icon={
                    Icon(when(label){"الرئيسية"->Icons.Default.Home;"مكتبتي"->Icons.Default.Folder;"استيراد مودات"->Icons.Default.Download;else->Icons.Default.Settings},null)
                },label={Text(label,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=RED,selectedTextColor=RED,indicatorColor=Color(0xFF35101D),unselectedIconColor=Color.White,unselectedTextColor=Color.White))
            }
        }
    }
}

@Composable
fun ModCard(mod:ModItem,onDetails:()->Unit,onPlay:()->Unit) {
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=PANEL),border=BorderStroke(1.dp,Color(0xFF4B2635))) {
        Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.End) {
            Box(Modifier.fillMaxWidth().height(78.dp).background(Brush.linearGradient(listOf(RED,Color(0xFF29102C))),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {
                Icon(Icons.Default.MusicNote,null,tint=Color.White,modifier=Modifier.size(44.dp))
            }
            Text(mod.name,fontWeight=FontWeight.Bold,color=Color.White,maxLines=1)
            Text("v${mod.version} • FNF ${mod.gameVersion}",fontSize=11.sp,color=MUTED)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick=onDetails,modifier=Modifier.weight(1f)) { Text("تفاصيل",fontSize=11.sp) }
                Button(onClick=onPlay,modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=RED)) { Text("تشغيل",fontSize=11.sp); Icon(Icons.Default.PlayArrow,null,Modifier.size(15.dp)) }
            }
        }
    }
}
