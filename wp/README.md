
```javascript
'use strict';
// TODO 最大化，update设置了文本未渲染，适配移动端，currentTitle
const baseUrl = "http://localhost:8080/";
const $body = $("body");
const $multiList = $("#multiList");
const $dialog = $("#dialog");
let token,userName;
// 创建未提交后无法编辑
let oldNoteId = -1;
/*
//弹出登录框
//全局存储token,userName
$dialog.dialog({
    closeOnEscape: false,
    modal: true,
    resizable: false,
    width: 330,
    position: {
        my: "top",
        at: "top",
        of: window
    },
    buttons: [
        {
            text: "Login",
            click: function() {
                //登录成功时调用关闭dialog
                //$( this ).dialog( "close" );
                getToken();
            }
        }
    ]
});
//ajax登录获取token
function getToken() {
    console.log("execute getToken");
    userName = $("#userName").val();
    let userWrap = {
        userName: userName,
        userPassword: $("#passWord").val()
    };
    $.ajax({
        url: baseUrl+'user',
        method: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        timeout: 10000,
        //The JSON.stringify() method converts a JavaScript object or value to a JSON string，满足json解析要求
        data: JSON.stringify(userWrap),
        success: function (responseData) {
            token = responseData.data;
            getList();
            $dialog.dialog("close");
        }
    });
}
// 设置分类列表公共方法
function setCategory(eachNote) {
    let categoriesChain = eachNote['categoriesChain'];
    let depth = 0;
    let $parentNote = $multiList;
    // 将/分割的列表路径存储在每个title中,update时设置path提取
    let categoriesPath = "";
    for (let i = categoriesChain.length - 1; i >= 0; i--) {
        depth++;
        let categoryId = categoriesChain[i].categoryId;
        let categoryName = categoriesChain[i].categoryName;
        categoriesPath += categoryName;
        if (i !== 0) {
            categoriesPath += "/";
        }
        // jquery获取到的dom为空，构造该dom
        let $oldParentNote = $parentNote;
        if (($parentNote = $("#category" + String(categoryId))).length === 0) {
            $parentNote = $oldParentNote;
            // Jquery()参数字符串生成dom
            let $currentNote = $("<div class=\"side-bar-mar\">");
            // attr键值为字符串
            $currentNote.attr("id", "category" + String(categoryId));
            $currentNote.attr("categoryName", categoryName);
            $currentNote.attr("expand",true);
            $currentNote.attr("depth",depth);
            $currentNote.append("<i class=\"far fa-minus-square plus-minus-position\"></i>");
            $currentNote.append("<a class=\"x-wiki-index-cat\">" + categoryName +"</a>");
            $parentNote.append($currentNote);
            $parentNote = $currentNote;
            // 下列函数返回完整的jquery对象，包括刚添加的dom
            // console.log($multiList.append($currentNote));
        }
    }
    let $currentNode1 = $("<div class=\"side-bar-mar\">");
    $currentNode1.attr("id", "title" + String(eachNote.id));
    $currentNode1.attr("depth", ++depth);
    $currentNode1.attr("categoriesPath", categoriesPath);
    $currentNode1.append("<a href=\"#\" class=\"x-wiki-index-item\">" + eachNote.title + "</a>");
    $parentNote.append($currentNode1);
}
//获取设置分类列表
function getList() {
    console.log("execute getList");
    $.ajax({
        url: baseUrl+userName+"/notes",
        method: 'GET',
        headers: {
            Authorization: token
        },
        contentType: 'application/json',
        dataType: 'json',
        timeout: 10000,
        success: function (responseData) {
            let code = responseData.code;
            console.log(code);
            if (code === 200) {
                // 设置multiList'content
                $multiList.append("<i class=\"far fa-minus-square plus-minus-position\"></i>");
                $multiList.append("<a class=\"x-wiki-index-cat\">content</a>");
                let data = responseData.data;
                for (let i = 0; i < data.length; i++) {
                    setCategory(data[i]);
                }
            }
        }
    });
}
// 解析content-editormd下的markdown文本
function toHtml() {
    console.log("execute toHtml");
    $(function () {
        editormd.markdownToHTML("content-editormd", {
            htmlDecode: "style,script,iframe", //可以过滤标签解码
            emoji: true,
            taskList: true,
            tex: true,               // 默认不解析
            flowChart: true,         // 默认不解析
            sequenceDiagram: true, // 默认不解析
            codeFold: true
        });
    });
}
// 获取设置文章
$body.on("click", ".x-wiki-index-item" ,function () {
    const $item = $(this);
    const $parentDiv = $item.parent();
    let title = $parentDiv.attr("id");
    let noteUrl = baseUrl + userName + "/notes/" + title.substring(5);
    console.log(noteUrl);
    $.ajax({
        url: noteUrl,
        method: 'GET',
        headers: {
            Authorization: token
        },
        contentType: 'application/json',
        dataType: 'json',
        timeout: 10000,
        success: function (responseData) {
            let noteId = responseData.data.noteId;
            let noteTitle = responseData.data.noteTitle;
            let noteContent = responseData.data.noteContent;
            //设置标题及文章
            $("#currentTitle").attr("noteId",noteId).text(noteTitle);
            oldNoteId = noteId;
            let $currentContent = $("<textarea style=\"display: none\"></textarea>");
            $("#store-current-content").text(noteContent);
            $currentContent.text(noteContent);
            $("#content-editormd").html($currentContent);
            toHtml();
        }
    });
});
//上传新建/修改的文章
$body.on("click", "#note-post-or-put" ,function () {
    // create / update sign
    let noteId = $("#currentTitle").attr("noteId");
    const categories = $("#category").val().split("/");
    const noteContent = $("#content-editormd-editor-doc").val();
    const noteTitle = $("#title").val();
    let addWrap = {
        title: noteTitle,
        content: noteContent,
        categories: categories
    };
    let noteUrl = baseUrl + userName + "/notes";
    let method = "POST";
    if (noteId !== "-1") {
        noteUrl += "/";
        noteUrl += noteId;
        method = "PUT";
    }
    console.log(noteUrl);
    $.ajax({
        url: noteUrl,
        method: method,
        headers: {
            Authorization: token
        },
        contentType: 'application/json',
        dataType: 'json',
        timeout: 10000,
        data: JSON.stringify(addWrap),
        success: function (responseData) {
            // 设置currentTitle,currentContent,currentTitle.noteId
            $("#currentTitle").attr("noteId",responseData.data.id).text(responseData.data.title);
            let $currentContent = $("<textarea style=\"display: none\"></textarea>");
            $("#store-current-content").text(noteContent);
            $currentContent.text(noteContent);
            //由append改为html替换，由于更换content渲染时append导致上次内容叠加（渲染后textarea别editormd删除了）
            $("#content-editormd").html($currentContent);
            toHtml();
            // 如果是更新文章，即分类列表中存在该标题，先删除
            if (noteId !== "-1") {
                // TODO 未考虑删除后该目录为空，并可能一直迭代根目录都为空
                $("#title" + noteId).remove();
            }
            setCategory(responseData.data);
            // 提交后不要刷新编辑器，可能只是中途保存
        }
    });
});
 */
//设置sidebar与row1高度占满，用于分割线
const $header = $("#header");
const $sidebar = $("#sidebar");
$("#row1").css("min-height", window.innerHeight - $header.height());
$sidebar.css("min-height", window.innerHeight - $header.height());
//全屏内容时position非float，可使用left弹出list
const $index = $("#index");
let startX,endX,startY,endY;
$body.on("touchstart",function (e) {
    //从 chrome56 开始，在 window、document 和 body 上注册的 touchstart 和 touchmove 事件处理函数，会默认为是 passive: true。
    //浏览器忽略 preventDefault() 就可以第一时间滚动了。
    //e.preventDefault();
    startX = e.originalEvent.changedTouches[0].pageX;
    startY = e.originalEvent.changedTouches[0].pageY;
});
$body.on("touchmove",function (e) {
    //e.preventDefault();
    if (window.innerWidth < 992) {
        endX = e.originalEvent.changedTouches[0].pageX;
        endY = e.originalEvent.changedTouches[0].pageY;
        let X = endX - startX;
        let Y = endY - startY;
        if (X > 0 && Math.abs(X) > Math.abs(Y)) {
            if ($index.attr('isHidden') === 'true') {
                $sidebar.animate({left : "0"});
                $("#content").animate({left : "65%"});
                $index.attr('isHidden', 'false');
            }
        }
        else if (X < 0 && Math.abs(X) > Math.abs(Y)) {
            if ($index.attr('isHidden') === 'false') {
                $sidebar.animate({left : "-65%"});
                $("#content").animate({left : "0"});
                $index.attr('isHidden', 'true');
            }
        }
    }
});
//所有加减号图标class点击缩放列表
/*
// 动态添加的元素绑定点击事件失效，on前面应该是在添加前存在的元素
// 选择器在第二参数位置
$(".plus-minus-position").on("click", function () {
    console.log("click");
    toggle(this);
});
 */
$body.on("click", ".plus-minus-position" ,function () {
    toggle(this);
});
//
function toggle(icon) {
    const $i = $(icon), $div = $i.parent(), expand = $div.attr('expand');
    if (expand === 'true') {
        collapseWikiNode(icon);
    } else {
        expandWikiNode(icon);
    }
}
//
function collapseWikiNode(icon, rec) {
    const $i = $(icon), $div = $i.parent();
    $div.attr('expand', 'false');
    $i.removeClass('fa-minus-square');
    $i.addClass('fa-plus-square');
    $div.find('>div').hide();
    if (rec) {
        $div.find('>div>i').each(function () {
            collapseWikiNode(this, rec);
        });
    }
}
//
function expandWikiNode(icon, rec) {
    const $i = $(icon), $div = $i.parent();
    $div.attr('expand', 'true');
    $i.removeClass('fa-plus-square');
    $i.addClass('fa-minus-square');
    $div.find('>div').show();
    if (rec) {
        $div.find('>div>i').each(function () {
            expandWikiNode(this, rec);
        });
    }
}
// 设置编辑框
function setEditor(markdown) {
    console.log("execute setEditor");
    editormd("content-editormd-editor", {
        markdown: markdown,
        width   : "90%",
        height  : "85%",
        syncScrolling : "single",
        path    : "lib/",
        saveHTMLToTextarea : false,//true则自动生成另一个textarea保存html的内容，则在同一个form中都会上传到后台
        imageUpload : true,
        imageFormats: ["jpg","jpeg","gif","png","bmp","webp"],
        imageUploadURL: "image",
        htmlDecode: "style,script,iframe", //可以过滤标签解码
        emoji: true,
        taskList: true,
        tex: true,               // 默认不解析
        flowChart: true,         // 默认不解析
        sequenceDiagram: true, // 默认不解析
        codeFold: true
    });
}
// 新建文章弹出编辑框
$body.on("click","#create", function () {
    $("#currentTitle").attr("noteId",-1);
    $("#title").attr("value", "");
    $("#category").attr("value", "");
    $("#overlay").css("display", "");
    // id为-1,创建，大于0，修改
    $("#markdownEditor").css("display", "");
    $("#content-editormd-editor").empty();
    $(setEditor(""));
    $(".editormd-preview-close-btn").css("display", "none");
});
// 修改文章弹出编辑框
$body.on("click","#edit", function () {
    let noteId = $("#currentTitle").attr("noteId");
    console.log(noteId);
    if (noteId !== "-1") {
        // 注意text(),html(),val()-->针对表单
        $("#title").attr("value", $("#currentTitle").text());
        $("#category").attr("value", $("#title" + noteId).attr("categoriesPath"));
        $("#overlay").css("display", "");
        // id为-1,创建，大于0，修改
        $("#markdownEditor").css("display", "");
        // 清空之前渲染nodes
        $("#content-editormd-editor").empty();
        $(setEditor($("#store-current-content").val()));
        // 关闭提前显示的预览关闭按钮
        $(".editormd-preview-close-btn").css("display", "none");
    }
});
// 关闭编辑框
$body.on("click","#editor-close", function () {
    $("#overlay").css("display", "none");
    $("#markdownEditor").css("display", "none");
    //
    if ($("#currentTitle").attr("noteId") === "-1") {
        $("#currentTitle").attr("noteId", oldNoteId);
    }
});

```

```css
@import url("https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css");

/*盒模型*/
* {
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;
}

/*覆盖或添加jquery ui dialog效果*/
.ui-widget-overlay {
    background-color: #98999a;
}

.ui-dialog {
    box-shadow: 1px 1px 3px 5px #898989;
}

.ui-dialog-titlebar {
    display: none;
}

/*a class鼠标悬浮时加深块体颜色*/
a.x-wiki-index-cat {
    color: #333333;
    font-size: large;
    display: block;
    padding: 3px 0 3px 0;
}

a.x-wiki-index-cat:hover {
    background: #f3f3f3;
}

a.x-wiki-index-item {
    color: #333333;
    text-decoration: none;
    display: block;
    font-size: medium;
    padding: 3px 0 3px 0;
}

a.x-wiki-index-item:hover {
    background: #f3f3f3;
}

/*每一级列表向右偏移*/
.side-bar-mar {
    position: relative;
    margin-left: 15px;
}

/*absolute脱离文档流加减号定位（左上角），相对父级非static（默认）元素,否则相对于body*/
.plus-minus-position {
    position: absolute;
    top: 10px;
    left: -16px;
    color: #b4b4b4;
}

/*相同命名属性叠加*/
.sidebar-para {
    border-right : solid 1px #e4e4e4;
}

@media (max-width: 600px) {
    /*原先fixed有效是由于：不为元素预留空间，而是通过指定元素相对于屏幕视口（viewport）的位置来指定元素位置。
元素的位置在屏幕滚动时不会改变。打印时，元素会出现在的每页的固定位置。fixed 属性会创建新的层叠上下文。
当元素祖先的 transform  属性非 none 时，容器由视口改为该祖先。*/
    .sidebar-para {
        position: absolute;
        left: -65%;
        width: 65%;
        padding-left : 1%;
        background: #fdfeff;
    }

    .content-para {
        position: relative;
        padding-left: 1%;
        padding-right: 1%;
        width: 100%;
    }
    /*编辑器*/
    #markdownEditor {
        position: fixed;
        z-index: 3;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #ffffff;
        box-shadow: 1px 1px 10px 5px #000000;
    }

}

/*992*/
@media (min-width: 600px) {
    .sidebar-para {
        float: left;
        padding-left: 1%;
        width: 16.7%;
        background: #fdfeff;
    }

    .content-para {
        float: left;
        padding-left: 1%;
        padding-right: 1%;
        width: 83.3%;
    }

    /*蒙层*/
    #overlay {
        position: fixed;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 1;
        background-color: rgba(0,0,0,0.1);
    }

    /*编辑器*/
    #markdownEditor {
        position: fixed;
        z-index: 2;
        top: 5%;
        left: 5%;
        width: 90%;
        height: 90%;
        background-color: #ffffff;
        box-shadow: 1px 1px 10px 5px #000000;
    }

    /*editor-above-functions*/
    #editor-above-functions {
        position: fixed;
        top: 5%;
        left: 5%;
        width: 90%;
        height: 5%;
    }

    /*content-editormd-editor*/
    #content-editormd-editor {
        position: fixed;
        top: 10%;
        left: 5%;
    }
}

/*输入框*/
.input {
    padding: 0 1em;
    text-align: center;
    height: 1.7em;
    border-radius: 10px;
}






```