let table, form, layer, layPage, flow, upload, laytpl;
let pageIndex = 1, pageSize = 8;
layui.use(['table', 'form', 'layer', 'laypage', 'flow', 'upload', 'laytpl'], function () {
    table = layui.table;
    form = layui.form;
    layer = layui.layer;
    layPage = layui.laypage;
    flow = layui.flow;
    upload = layui.upload;
    laytpl = layui.laytpl;
    queryTable();
    initUpload();

    // 查询
    $("#queryBtn").click(function () {
        queryTable();
    });

    $("#tableBox").on("click", ".img-box", function () {
        parent.selectImg($(this).children("img").attr("src"),$(this).children("img").attr("alt"));
        parent.layer.close(parent.layer.getFrameIndex(window.name));
    });
});

/**
 * 初始化页面数据
 */
function queryTable() {
    $.ajax({
        type: "POST",
        url: "/admin/attach/list",
        contentType: "application/json",
        data: JSON.stringify({
            pageSize,
            pageIndex,
            form: {
                name: $("#name").val(),
                type: "img"
            }
        }),
        success: function (data) {
            if (data.code === 200) {
                laytpl($("#tableTpl").html()).render(data.data, function (html) {
                    $("#tableBox").html(html);
                });
                layPage.render({
                    elem: 'tabBoxPage',
                    limit: pageSize,
                    count: data.total,
                    curr: data.pageIndex,
                    layout: ['count', 'prev', 'page', 'next'],
                    jump: function (obj, first) {
                        pageIndex = obj.curr;
                        pageSize = obj.limit;
                        //首次不执行
                        if (!first) {
                            queryTable();
                        }
                    }
                });

                flow.lazyimg();
            } else {
                layer.msg(data.msg, {icon: 2});
            }
        },
        error: function (data) {
            layer.msg("加载列表失败", {icon: 2});
        }
    })
}

/**
 * 初始化上传
 */
function initUpload() {
    let loadIndex;
    upload.render({
        elem: '#uploadBtn'
        , url: '/admin/attach/upload'
        , accept: 'file'
        , acceptMime: 'image/*,image/ico'
        , exts: 'ico|jpg|png|gif|bmp|jpeg|tif|svg'
        ,choose: function (obj) {
            loadIndex = layer.load();
        }
        , done: function (res) {
            layer.close(loadIndex);
            if (res.code === 200) {
                parent.layer.close(parent.layer.getFrameIndex(window.name));
                parent.selectImg(res.data.url);
            } else {
                layer.msg(res.msg, {icon: 2});
            }
        }
        , error: function () {
            layer.close(loadIndex);
            layer.msg("上传失败", {icon: 2});
        }
    });
}