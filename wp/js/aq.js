'use strict';

/**
 * sessionStorage 有效期在当前会话
 */

const isDebug = false;
//const baseUrl = "http://localhost:8080/";
const baseUrl = "https://oscloud.xyz/wp/";
const $body = $("body");
const $multiList = $("#multiList");
const $dialog = $("#dialog");
const $currentTitle = $("#currentTitle");
const $header = $("#header");
const $sidebar = $("#sidebar");
const $index = $("#index");
const $editor = $("#content-editormd-editor");
const $searchContainer = $("#search-container");
const $searchInfo = $("#search-info");
// 全局存储token,userName
let token,userName;
// 直接关闭窗口，未上传，重置NoteID（由于每次create都将noteId置为-1，保证create时url为post而非put）
let oldNoteId = "-1";
// editor object
let wpEditor;
// cache current notes by searchInfo
let notes;

// pre
pre();
// bind enter for login first
$body.on("keydown",function (event) {
    if (event.keyCode === 13) {
        $('.ui-button.ui-corner-all.ui-widget').click();
    }
});
// login in
login();
// 获取设置note
$body.on("click", ".x-wiki-index-item" ,function () {
    const $item = $(this);
    const $parentDiv = $item.parent();
    let title = $parentDiv.attr("id");
    let noteUrl = baseUrl + userName + "/notes/" + title.substring(5);
    if (isDebug) console.log(noteUrl);
    if (token === null) {
        alert("please login");
        return;
    }
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
            if (responseData.code === 200) {
                let noteId = responseData.data.noteId;
                let noteTitle = responseData.data.noteTitle;
                let noteContent = responseData.data.noteContent;
                //设置标题及文章
                $currentTitle.attr("noteId",noteId).text(noteTitle);
                oldNoteId = noteId;
                let $currentContent = $("<textarea style=\"display: none\"></textarea>");
                $("#store-current-content").val(noteContent);
                $currentContent.val(noteContent);
                $("#content-editormd").html($currentContent);
                toHtml();
                // 对桌面端的css无效，故无需判断window.width
                if ($index.attr('isHidden') === 'false') {
                    $sidebar.animate({left : "-65%"});
                    $("#content").animate({left : "0"});
                    $index.attr('isHidden', 'true');
                }
            }
        },
        error: function (jqXHR,textStatus) {
            alert(`${textStatus}: ${jqXHR.responseJSON.msg}`);
        }
    });
});
// 点击"WP"弹出列表收回列表,注意on参数select只能是string
$body.on("click", "#index",function () {
    if (window.innerWidth < 992) {
        if ($index.attr('isHidden') === 'true') {
            $sidebar.animate({left : "0"});
            $("#content").animate({left : "65%"});
            $index.attr('isHidden', 'false');
        } else {
            $sidebar.animate({left : "-65%"});
            $("#content").animate({left : "0"});
            $index.attr('isHidden', 'true');
        }
    }
});
// index.isHidden=false时点击content收回列表
$body.on("click", "#content",function () {
    if ($index.attr("isHidden") === "false") {
        $sidebar.animate({left : "-65%"});
        $("#content").animate({left : "0"});
        $index.attr('isHidden', 'true');
    }
});
/* 所有加减号图标class点击缩放列表
* 动态添加的元素绑定点击事件失效，on前面应该是在添加前存在的元素
* 选择器在第二参数位置
$(".plus-minus-position").on("click", function () {
    console.log("click");
    toggle(this);
});
 */
$body.on("click", ".plus-minus-position" ,function () {
    toggle(this);
});
// 所有分类文字点击缩放列表
$body.on("click", ".x-wiki-index-cat",function () {
    toggle($(this).prev().get(0));
});
// 登出
$body.on("click","#logout",function () {
    token = null;
    notes = null;
    $("#backup").attr("href","#");
    sessionStorage.removeItem('password');
    sessionStorage.removeItem('hasLogin');
    $("#content-editormd").empty();
    $multiList.empty();
    $currentTitle.empty();
    $searchContainer.empty();
    $searchContainer.css("display","none");
    alert("already logout!!!");
});
// 剪切板监听
$body.on("paste",function (event) {
    let items = event.originalEvent.clipboardData.items;
    let file = null;
    if (items && items.length) {
        // 搜索剪切板items
        for (let i = 0; i < items.length; i++) {
            // String 查询字串，否则返回-1
            if (items[i].type.indexOf('image') !== -1) {
                file = items[i].getAsFile();
                break;
            }
        }
    } else {
        alert("当前浏览器不支持");
        return;
    }
    if (!file) {
        if(isDebug) alert("粘贴内容非图片");
        return;
    }
    preview(file,true);
});
// 新建文章弹出编辑框
$body.on("click","#create", function () {
    $currentTitle.attr("noteId", "-1");
    $("#overlay").css("display", "");
    // id为-1,创建，大于0，修改
    $("#content-editormd-editor").css("display", "");
    $("#content-editormd-editor").empty();
    if (window.innerWidth > 600) {
        $(setEditor(""));
    } else {
        $(setMinEditor(""));
    }
});
// 修改文章弹出编辑框
$body.on("click","#edit", function () {
    let noteId = $currentTitle.attr("noteId");
    if (noteId !== "-1") {
        $("#overlay").css("display", "");
        // id为-1,创建，大于0，修改
        $("#content-editormd-editor").css("display", "");
        // 清空之前渲染nodes
        $("#content-editormd-editor").empty();
        if (window.innerWidth > 600) {
            $(setEditor($("#store-current-content").val()));
        } else {
            $(setMinEditor($("#store-current-content").val()));
        }
    }
});
// 监听搜索框输入清空，清除搜索结果
$body.on("input","#search-info", function () {
    if ($searchContainer.val() === "") {
        $searchContainer.css("display","none");
    }
});
// search button
$body.on("click", "#search-button", search);
// 点击查询结果item设置其内容
$body.on("click",".search-result-item",function () {
    let noteLocation = $(this).attr("searchNoteId");
    let noteId = notes[noteLocation].id;
    let noteTitle = notes[noteLocation].title;
    let noteContent = notes[noteLocation].content;
    //设置标题及文章
    $currentTitle.attr("noteId",noteId).text(noteTitle);
    oldNoteId = noteId;
    let $currentContent = $("<textarea style=\"display: none\"></textarea>");
    $("#store-current-content").val(noteContent);
    $currentContent.val(noteContent);
    $("#content-editormd").html($currentContent);
    toHtml();
});


// 编辑器自定义tool bar
function setEditor(markdown) {
    wpEditor = editormd("content-editormd-editor", {
        markdown: markdown,
        theme: "dark",
        //previewTheme : "dark",
        //editorTheme : "pastel-on-dark",
        tocm: true,
        width   : "90%",
        height  : "90%",
        syncScrolling : true,// true -> bisync, false -> disabled, "single" -> Only editor area sync
        path    : "lib/",
        saveHTMLToTextarea : false,//true则自动生成另一个textarea保存html的内容，则在同一个form中都会上传到后台
        //imageUpload : true,
        //imageFormats: ["jpg","jpeg","gif","png","bmp","webp"],
        //imageUploadURL: "image",
        htmlDecode: "style,script,iframe", //可以过滤标签解码
        emoji: true,
        taskList: true,
        tex: true,               // 默认不解析
        flowChart: true,         // 默认不解析
        sequenceDiagram: true, // 默认不解析
        codeFold: true,
        // 自定义toolbar
        toolbarIcons : function() {
            // Or return editormd.toolbarModes[name]; // full, simple, mini
            // Using "||" set icons align right.
            return [
                "undo", "redo", "|",
                "bold", "del", "italic", "quote", "ucwords", "uppercase", "lowercase", "|",
                "h1", "h2", "h3", "h4", "h5", "h6", "|",
                "list-ul", "list-ol", "hr", "|",
                "link", "reference-link", "code", "preformatted-text", "code-block", "table", "datetime", "emoji", "html-entities", "pagebreak", "|",
                "goto-line", "watch", "preview", "fullscreen", "clear", "search", "|",
                "help", "info",
                "title","category","postIcon", "imageIcon","deleteIcon","||", "closeIcon"];
        },
        toolbarIconsClass : {
            postIcon : "fa-caret-square-up",
            imageIcon : "fa-image",
            deleteIcon : "fa-trash",
            closeIcon : "fa-window-close"  // 指定一个FontAawsome的图标类
        },
        // 用于增加自定义工具栏的功能，可以直接插入HTML标签，不使用默认的元素创建图标
        toolbarCustomIcons : {
            title   : "<input id=\"title\" type=\"text\" class=\"input\" placeholder=\"title\" >",
            category :   "<input id=\"category\" type=\"text\" class=\"input\" placeholder=\"category\">"
        },

        lang : {
            toolbar : {
                file : "upload file",
                postIcon : "upload note",  // 自定义按钮的提示文本，即title属性
                imageIcon : "upload image",
                deleteIcon : "delete note",
                closeIcon: "close this editor"
            }
        },

        // 自定义工具栏按钮的事件处理
        toolbarHandlers : {
            postIcon : postOrPutNote,
            imageIcon : chooseImage,
            deleteIcon : deleteNote,
            closeIcon : function () {
                // 全屏编辑后未退出，直接关闭窗口导致首页无法滚动，故关闭窗口前先退出全屏
                wpEditor.fullscreenExit();
                $("#overlay").css("display", "none");
                $editor.css("display", "none");
                $currentTitle.attr("noteId", oldNoteId);
            }
        },

        // 加载完成回调事件
        onload : onLoad,

        // 全屏回调事件，这里主要调整left top值
        onfullscreen : function() {
            if (window.innerWidth > 600) {
                $editor.css("top",0).css("left",0);
            }
        },

        onfullscreenExit : function() {
            if (window.innerWidth > 600) {
                $editor.css("top","5%").css("left","5%");
            }
        }
    });
}
// 移动端编辑器自定义toolbar
function setMinEditor(markdown) {
    wpEditor = editormd("content-editormd-editor", {
        markdown: markdown,
        theme: "dark",
        //previewTheme : "dark",
        //editorTheme : "pastel-on-dark",
        tocm: true,
        width   : "100%",
        height  : "100%",
        syncScrolling : true,// true -> bisync, false -> disabled, "single" -> Only editor area sync
        path    : "lib/",
        saveHTMLToTextarea : false,//true则自动生成另一个textarea保存html的内容，则在同一个form中都会上传到后台
        htmlDecode: "style,script,iframe", //可以过滤标签解码
        emoji: true,
        taskList: true,
        tex: true,               // 默认不解析
        flowChart: true,         // 默认不解析
        sequenceDiagram: true, // 默认不解析
        codeFold: true,
        // 自定义toolbar
        toolbarIcons : function() {
            // Or return editormd.toolbarModes[name]; // full, simple, mini
            // Using "||" set icons align right.
            return [
                "title","category",
                "watch", "preview", "search", "code-block", "table", "datetime",
                "postIcon","deleteIcon",
                "||", "closeIcon"];
        },
        toolbarIconsClass : {
            postIcon : "fa-caret-square-up",
            deleteIcon : "fa-trash",
            closeIcon : "fa-window-close"  // 指定一个FontAawsome的图标类
        },
        // 用于增加自定义工具栏的功能，可以直接插入HTML标签，不使用默认的元素创建图标
        toolbarCustomIcons : {
            //file   : "<input type=\"file\" accept=\".md\"/>",
            title   : "<input id=\"title\" type=\"text\" class=\"input\" placeholder=\"title\" >",
            category :   "<input id=\"category\" type=\"text\" class=\"input\" placeholder=\"category\">"
        },

        lang : {
            toolbar : {
                file : "upload file",
                postIcon : "upload note",  // 自定义按钮的提示文本，即title属性
                deleteIcon : "delete note",
                closeIcon: "close this editor"
            }
        },

        // 自定义工具栏按钮的事件处理
        toolbarHandlers : {
            postIcon : postOrPutNote,
            deleteIcon : deleteNote,
            closeIcon : function () {
                $("#overlay").css("display", "none");
                $editor.css("display", "none");
                $currentTitle.attr("noteId", oldNoteId);
            }
        },

        // 加载完成回调事件
        onload : onLoad,
    });
}
// collapse or expand list
function toggle(icon) {
    const $i = $(icon), $div = $i.parent(), expand = $div.attr('expand');
    if (expand === 'true') {
        collapseWikiNode(icon);
    } else {
        expandWikiNode(icon);
    }
}
// collapse
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
// expand
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
// 设置分类列表公共方法
function setCategory(eachNote) {
    let categoriesChain = eachNote['categoriesChain'];
    let depth = 0;
    let $parentNote = $multiList;
    // 将/分割的列表路径以字符串存储在每个title中,Edit初始化编辑器后直接提取路径值
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
            $currentNote.attr("depth",depth);
            $currentNote.attr("expand",true);
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
// 获取设置分类列表
function getList() {
    if (isDebug) console.log("execute getList");
    $.ajax({
        url: `${baseUrl}${userName}/notes?searchInfo`,
        method: 'GET',
        headers: {
            Authorization: token
        },
        dataType: 'json',
        data: "",
        timeout: 10000,
        success: function (responseData) {
            let code = responseData.code;
            if (isDebug) console.log(code);
            if (code === 200) {
                // 设置multiList'content
                $multiList.append("<i class=\"far fa-minus-square plus-minus-position\"></i>");
                $multiList.append("<a class=\"x-wiki-index-cat\">content</a>");
                let data = responseData.data;
                for (let i = 0; i < data.length; i++) {
                    setCategory(data[i]);
                }
                // 默认打开一级目录（先全收缩，再打开一级）
                let icon = $('#multiList>i').get(0);
                collapseWikiNode(icon, true);
                toggle(icon);
            }
        }
    });
}
// 解析content-editormd下的markdown文本（参数超过3层无法传递?）
function toHtml() {
    if (isDebug) console.log("execute toHtml");
    $(function () {
        editormd.markdownToHTML("content-editormd", {
            tocm: true,
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
// 设置dialog预览图片并上传
function preview(file,isScreenShot) {
    let reg = /(.*)\.(jpg|jpeg|gif|png|PNG|bmp|webp)$/;
    if (!reg.test(file.name)) {
        alert("wrong file type!!!");
        $("#imageChoose")[0].value = '';
        return;
    }
    let reader = new FileReader();
    // fileReader readAsUrl need time, so onload
    reader.onload = function () {
        let base64Url = reader.result;
        let img = new Image();
        img.src = base64Url;
        // Image load base64 source need time, so onload
        img.onload = function() {
            let width = img.width;
            let height = img.height;
            let base = 500;
            let maxBase = width < height ? height : width;
            width = width * base / maxBase;
            height = height * base / maxBase;
            const $preview = $('<img>');
            // 创建dialog进行预览
            $preview.dialog({
                closeOnEscape: false,
                modal: true,
                width: width + 10,
                height: height + 100,
                buttons: [
                    {
                        text: "cancel",
                        click: function() {
                            $preview.dialog('destroy');
                        }
                    },
                    {
                        text: "upload",
                        click: function() {
                            let formData = new FormData();
                            if (isScreenShot) {
                                // 截图默认文件名均为image.png,修改为时间命名
                                file = new File([file], new Date().getTime()+".png",{type:"image/png"});
                            } else {
                                // 文件名添加时间前缀，防止同名文件覆盖
                                file = new File([file], new Date().getTime() + '-' + file.name, {type: file.type});
                            }
                            formData.append('image',file);
                            $.ajax({
                                url: baseUrl + userName + "/image",
                                method: "POST",
                                headers: {
                                    Authorization: token
                                },
                                dataType: 'json',
                                timeout: 10000,

                                // form data
                                data: formData,

                                // jQuery会将data对象转换为字符串来发送HTTP请求，
                                // 默认情况下会用 application/x-www-form-urlencoded编码来进行转换
                                // 上传二进制数据需禁用
                                enctype: 'multipart/form-data',
                                processData: false,
                                contentType: false,

                                success: function (responseData) {
                                    if (responseData.code === 200) {
                                        wpEditor.insertValue("![](" + responseData.data + ")\n");
                                        $preview.dialog('destroy');
                                        if (isDebug) console.log("preview destroy");
                                    }
                                }
                            });
                        }
                    }
                ]
            });
            // dialog会修改下列属性，故须在dialog之后才能覆盖
            $preview.attr("src",base64Url).css("width",width,"height",height);
        };
    };
    reader.readAsDataURL(file);
}
/**
 * @param {Object}      cm         CodeMirror对象
 * @param {Object}      icon       图标按钮jQuery元素对象
 * @param {Object}      cursor     CodeMirror的光标对象，可获取光标所在行和位置
 * @param {String}      selection  编辑器选中的文本
 */
function postOrPutNote(cm, icon, cursor, selection) {
    // create / update sign
    let noteId = $currentTitle.attr("noteId");
    let category = $("#category").val();
    if (category.length === 0 || category === "/") {
        alert("category should not empty!!!")
        return;
    }
    const categories = category.split("/");
    // 通过编辑器中的textarea的name属性获取相应noteContent
    const noteContent = $("[name=content-editormd-editor-markdown-doc]").val();
    if (isDebug) console.log(noteContent);
    const noteTitle = $("#title").val();
    if (noteTitle.length === 0) {
        alert("title should not empty!!!");
        return;
    }
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
    if (isDebug) console.log(noteUrl);
    if (token === null) {
        alert("please login");
        return;
    }
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
            if (responseData.code === 200) {
                // 设置currentTitle,currentContent,currentTitle.noteId
                $currentTitle.attr("noteId",responseData.data.id).text(responseData.data.title);
                oldNoteId = responseData.data.id;
                // 渲染后textarea被editormd删除，且由于toHtml函数无法三层传递参数，故每次新建textarea节点
                let $currentContent = $("<textarea style=\"display: none\"></textarea>");
                $("#store-current-content").val(noteContent);
                $currentContent.val(noteContent);
                //由append节点改为html替换，由于append导致内容叠加而非替换
                $("#content-editormd").html($currentContent);
                if (isDebug) console.log($currentContent.val());
                toHtml();
                // 更新列表
                // 如果是更新文章，即分类列表中存在该标题，先删除
                if (noteId !== "-1") {
                    removeToUp($("#title" + noteId));
                }
                setCategory(responseData.data);
                alert("note already post!!!");
            }
        },
        error: function (jqXHR,textStatus) {
            alert(`${textStatus}: ${jqXHR.responseJSON.msg}`);
        }
    });
}
// deleteNote
function deleteNote() {
    let noteId = $currentTitle.attr("noteId");
    let deleteUrl = baseUrl + userName + "/notes/" + noteId;
    if (isDebug) console.log(deleteUrl);
    if (token === null) {
        alert("please login");
        return;
    }
    $.ajax({
        url: deleteUrl,
        method: "DELETE",
        headers: {
            Authorization: token
        },
        contentType: 'application/json',
        dataType: 'json',
        timeout: 10000,
        success: function (responseData) {
            if (responseData.code === 200) {
                removeToUp($("#title" + noteId));
                $("#content-editormd").empty();
                $currentTitle.empty().attr("noteId", "-1");
                alert("already delete note " + noteId);
            }
        },
        error: function (jqXHR,textStatus) {
            alert(`${textStatus}: ${jqXHR.responseJSON.msg}`);
        }
    });
}
// 编辑器加载完成回调
function onLoad(){
    let noteId = $currentTitle.attr("noteId");
    if (noteId !== "-1") {
        // 注意text(),html(),val()-->针对表单
        $("#title").attr("value", $currentTitle.text());
        $("#category").attr("value", $("#title" + noteId).attr("categoriesPath"));
    }
    // 关闭提前显示的预览关闭按钮
    $(".editormd-preview-close-btn").css("display", "none");
}
// 删除当前节点，父节点为空迭代删除
function removeToUp($currentItem) {
    let $parent = $currentItem.parent();
    // 到达根节点
    if ($parent.attr("id") === $multiList.attr("id")) {
        // 3-->父节点下2子节点描述父节点本身+currentItem
        if ($parent.children().length === 3) {
            $parent.empty();
        } else {
            $currentItem.remove();
        }
    } else {
        if ($parent.children().length === 3) {
            $parent.empty();
            removeToUp($parent);
        } else {
            $currentItem.remove();
        }
    }
}
// 选择上传的图片
function chooseImage() {
    if (isDebug) console.log("---------get in chooseImage function--------");
    $body.on("change", "#imageChoose",function () {
        let file = this.files[0];
        preview(file,false);
        // 最后将input元素的value清空，防止下次相同文件不触发change事件
        this.value = '';
    });
    $("#imageChoose").click();
}
// search
function search() {
    if (isDebug) console.log(`step into search function`);
    let searchInfo = $searchInfo.val();
    if (searchInfo === "") {
        alert("searchInfo should not empty!!!");
        return;
    }
    $.ajax({
        url: `${baseUrl}${userName}/notes?searchInfo=${searchInfo}`,
        method: 'GET',
        headers: {
            Authorization: token
        },
        dataType: 'json',
        timeout: 10000,
        success: function (responseData) {
            let code = responseData.code;
            if (isDebug) console.log(code);
            if (code === 200) {
                $searchContainer.empty();
                $searchContainer.css("display","block");
                notes = responseData.data;
                let $searchResultInfo = $("<h5 class='search-result-info'>");
                $searchResultInfo.text(`${notes.length} matching`);
                $searchContainer.append($searchResultInfo);
                $searchContainer.append($("<hr>"));
                for (let i = 0; i < notes.length; i++) {
                    let $searchNoteItem = $("<a href=\"javascript:\" class=\"search-result-item\">");
                    $searchNoteItem.text(notes[i].title);
                    $searchNoteItem.attr("searchNoteId",i);
                    $searchContainer.append($searchNoteItem);
                }
            } else if (code === 404) {
                $searchContainer.empty();
                $searchContainer.css("display","block");
                let $searchResultInfo = $("<h5 class='search-result-info'>");
                $searchResultInfo.text(`nothing matching`);
                $searchContainer.append($searchResultInfo);
                $searchContainer.append($("<hr>"));
            }
        },
        error: function (jqXHR,textStatus) {
            alert(`${textStatus}: ${jqXHR.responseJSON.msg}`);
        }
    });
}
// pre
function pre() {
    // 设置sidebar与row1高度占满，用于分割线
    $("#row1").css("min-height", window.innerHeight - $header.height());
    $sidebar.css("min-height", window.innerHeight - $header.height());
}
// login
function login() {
    userName = sessionStorage.getItem('username');
    let password = sessionStorage.getItem('password');
    if (userName !== null && password !== null) {
        getToken(userName,password);
    } else {
        // 弹出登录框
        $dialog.dialog({
            closeOnEscape: false,
            modal: true,
            resizable: false,
            width: 239,
            position: {
                my: "top",
                at: "top",
                of: window
            },
            buttons: [
                {
                    text: "Login",
                    click: function() {
                        userName = $("#userName").val();
                        let password = $("#passWord").val();
                        sessionStorage.setItem('username',userName);
                        sessionStorage.setItem('password',password);
                        $("#passWord").val("");
                        //登录成功时调用关闭dialog
                        getToken(userName,password);
                    }
                }
            ]
        });
    }
}
// ajax登录获取token
function getToken(userName,password) {
    if (isDebug) console.log("execute getToken");
    let userWrap = {
        userName: userName,
        userPassword: password
    };
    $.ajax({
        url: baseUrl+'token',
        method: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        timeout: 10000,
        //The JSON.stringify() method converts a JavaScript object or value to a JSON string，满足json解析要求
        data: JSON.stringify(userWrap),
        success: function (responseData) {
            if (responseData.code === 200) {
                token = responseData.data;
                // 设置backUrl
                let backUpUrl = `${baseUrl}${userName}/backupfile?token=${token}`;
                $("#backup").attr("href",backUpUrl);
                getList();
                if (sessionStorage.getItem('hasLogin') === null) {
                    //登录成功时调用关闭dialog
                    $dialog.dialog("close");
                    sessionStorage.setItem('hasLogin','hasLogin');
                    $body.off('keydown');
                }
            }
        },
        error: function (jqXHR,textStatus) {
            //TODO 未知原因，似乎dialog input 获得焦点后回车，juqeryui内部逻辑自动关闭dailog
            $dialog.dialog("open");
            alert(`${textStatus}: ${jqXHR.responseJSON.msg}`);
        }
    });
}