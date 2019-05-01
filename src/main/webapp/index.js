$(function () {

    var skip = 0;
    var rowsPerPage = 3;

    createSessionCart();
    printProducts(skip, rowsPerPage);

    $('.mar-invisible').hide();
    $('#mar-signOutButton').hide();
    $('#mar-cartView').hide();
    $('#mar-adminButton').hide();

    // [<<]
    $('#prev').click(function () {
        skip -= rowsPerPage;
        if (skip < 0) skip = 0;
        printProducts(skip, rowsPerPage);
    });
    // [>>]
    $('#next').click(function () {
        skip += rowsPerPage;
        printProducts(skip, rowsPerPage);
    });

    // [Show Cart]
    $('#mar-showCart').click(function () {
        $('#mar-productView').hide();
        $('#mar-cartListTbody').html('');
        $('#mar-cartView').show();
        printCart()
    });

    // [Show Products]
    $('#mar-showProduct').click(function () {
        $('#mar-cartView').hide();
        $('#mar-productView').show();
    });

    // [Buy]
    $('#mar-buyButton').click(function () {
        $('#mar-cartView').hide();
        $('#mar-productView').show();
        buy()
    });

    // [Sign In]
    $('#mar-signInButton').click(function () {
        $('#mar-loginModal').modal('show');
    });
    // [Sign In - Login button]
    $('#mar-loginButton').click(function () {
        login($('#username').val(), $('#password').val());
    });
    // [Sign In - New User button]
    $('#mar-newUserButton').click(function () {
        newUser($('#username').val(), $('#password').val());
    });

    // [Sign Out]
    $('#mar-signOutButton').click(function () {
        logout();
    });
    // [Admin]
    $('#mar-adminButton').click(function () {
        printOrderList();
    });
});

function createSessionCart() {

    $.ajax({
        url: 'api/cart/getsessioncart',
        method: 'GET',
        dataType: 'json'
    }).done(function () {
        console.log('SESIJOS KREPSELIS SUKURTAS');
    }).fail(function () {
        console.log('SESIJOS KREPSELIS NE SUKURTAS');
    });
}

// PRODUCTS ------------------------------------------------------------------------------------------------------------
function printProducts(skip, rowsPerPage) {

    $.ajax({
        url: 'api/product/list',
        method: 'GET',
        dataType: 'json',
        data: {
            size: rowsPerPage + 1,
            skip: skip
        }
    }).done(function (data) {

        if (data.length <= rowsPerPage) $('#next').hide();
        else $('#next').show();

        bildHtmlProductsRows(data, rowsPerPage);

    }).fail(function () {
        console.log("PRODUKTAI NEATSPAUSDINTI");
    });
}

function bildHtmlProductsRows(products, rowsPerPage) {

    var html = '';
    for (var i = 0; i < Math.min(products.length, rowsPerPage); i++) {

        html += '<tr class="ml-product">';
        html += ' <td class="text-right"><img src="' + products[i].image + '" alt="Responsive image" class="img-fluid" /></td>';
        html += ' <td>' + products[i].name + '</td>';
        html += ' <td class="text-right">' + products[i].price.toLocaleString() + '</td>';
        html += ' <td class="text-right">';
        html += '  <a href="#" class="nav-link btn btn-info btn-sm" ' +
            'onclick="jamam(' + products[i].id + ')">';
        html += '   <span class="glyphicon glyphicon-shopping-cart"></span> Add to Cart</a>';
        html += ' </td>';
        html += '</tr>';
    }
    $('#mar-productListTbody').html(html);
}

// CART ----------------------------------------------------------------------------------------------------------------
function jamam(productId) {

    var token = window.localStorage.token;
    // dedama į 1 krepseli
    $.ajax({
        url: 'api/cart/jamam',
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        headers: {Authorization: "Bearer " + token},
        data: JSON.stringify({
            id: productId,
            qty: 1
        })
    }).done(function () {
        console.log('PREKE ĮDĖTA Į KREPŠELĮ');
        printCart();
    }).fail(function () {
        console.log('PREKE NE ĮDĖTA Į KREPŠELĮ');
    });
}

function printCart() {

    $.ajax({
        url: 'api/cart/getsessioncart',
        method: 'GET',
        dataType: 'json'
    }).done(function (cart) {
        bildHtmlCartRows(cart);
    }).fail(function () {
        console.log("CART NEATSPAUSDINTA");
    });
}

function bildHtmlCartRows(cart) {

    var cartLines = cart.cartLines;

    var html = '';
    for (var i = 0; i < cartLines.length; i++) {

        html += addHtmlCartRow(cartLines[i].id, cartLines[i].qty, cartLines[i].product);
    }

    $('#mar-cartListTbody').html(html);
    $('#mar-totalSum').html(cart.total.toLocaleString());
    $('#mar-totalSumRespons').html(cart.total.toLocaleString());
    $('.mar-invisible').hide();

    $('.mar-refreshCartLine').on('click', function () {

        var oldQty = Number($(this).closest('tr').find('input')[0].value);
        var productId = Number($(this).closest('tr').find('td')[0].innerHTML);
        updateCartLine(productId, oldQty);

    });
}

function addHtmlCartRow(cartLineId, cartLineQty, product) {

    var html = '<tr>';
    html += ' <td class="mar-invisible mar-cartLineProductId">' + product.id + '</td>';
    html += ' <td data-th="Product">';
    html += '   <div class="row">';
    html += '     <div class="col-sm-2 hidden-xs"><img src="' + product.image + '"  alt="..." class="img-fluid"/></div>';
    html += '     <div class="col-sm-10">';
    html += '       <h4 class="nomargin">' + product.name + '</h4>';
    html += '       <p>' + '</p>'; // dscription place
    html += '     </div>';
    html += '   </div>';
    html += ' </td>';
    html += ' <td data-th="Price" class="text-right">€ ' + product.price.toLocaleString() + '</td>';
    html += ' <td data-th="Quantity" class="text-right"><input type="number" class="form-control" value="' + cartLineQty + '"></td>';
    html += ' <td data-th="Subtotal" class="text-right">€ ' + (product.price * cartLineQty).toLocaleString() + '</td>';
    html += ' <td class="actions text-right" data-th="">';
    html += '   <button class="btn btn-info btn-sm mar-refreshCartLine"><i class="fa fa-refresh"></i></button>';
    html += '   <button class="btn btn-danger btn-sm" ' + 'onclick="deleteCartLine(' + product.id + ')"><i class="fa fa-trash-o"></i></button>';
    html += ' </td>';
    html += '</tr>';
    return html;
}

// UPDATE --------------------------------------------------------------------------------------------------------------
function updateCartLine(productId, oldQty) {

    if (oldQty < 0) oldQty = 0;

    $.ajax({
        url: 'api/cart/updateCartLine/' + productId + '/' + oldQty,
        method: 'PUT',
        dataType: 'json',
        contentType: 'application/json',
        data: {}
    }).done(function () {
        console.log("CART LINE ATNAUJINTA");
        printCart();
    }).fail(function () {
        console.log("CART LINE NE ATNAUJINTA");
    });
}

// DELETE --------------------------------------------------------------------------------------------------------------
function deleteCartLine(productId) {

    $.ajax({
        url: 'api/cart/deleteCartLine/' + productId,
        method: 'DELETE',
        dataType: 'json',
        contentType: 'application/json'
    }).done(function () {
        console.log("CART LINE ISTRINTA");
        printCart();
    }).fail(function () {
        console.log("CART LINE NE ISTRINTA");
    });
}

// LOGIN ---------------------------------------------------------------------------------------------------------------
function login(username, password) {

    $.ajax({
        url: 'api/auth/login',
        method: 'POST',
        dataType: 'json',
        Accept: 'application/json',
        contentType: 'application/json',
        data: JSON.stringify({
            username: username,
            password: password
        })
    }).done(function (data) {
        console.log('PRISILOGINTA');

        window.localStorage.token = data.token;

        console.log('data.role=' + data.role);
        console.log('data.token=' + data.token);

        synchronizeCarts(username);

        if (data.role === "admin") {
            $('#mar-adminButton').show();
        }

        $('#mar-signInButton').hide();
        $('#mar-signOutButton').show();
        $('#mar-loginModal').modal('hide');
        $('#mar-loggedUserName').text('User: ' + username);

    }).fail(function () {
        $('#mar-loginModal').modal('hide');
        console.log('NE PRISILOGINTA');
        alert('Neprisijungta, neteisingi duomenys!');
    });
}

function newUser(username, password) {

    $.ajax({
        url: 'api/auth/newuser',
        method: 'POST',
        dataType: 'json',
        Accept: 'application/json',
        contentType: 'application/json',
        data: JSON.stringify({
            username: username,
            password: password,
            role: "user"
        })
    }).done(function (data) {
        console.log('SUKURTAS NEW USER ' + username + '-' + password);

        window.localStorage.token = data.token;

        synchronizeCarts(username);

        $('#mar-signInButton').hide();
        $('#mar-signOutButton').show();
        $('#mar-loginModal').modal('hide');
        $('#mar-loggedUserName').text('New User: ' + username);

    }).fail(function () {
        $('#mar-loginModal').modal('hide');
        alert('NE SUKURTAS NEW USER');
    });
}

function logout() {

    keepUserCartInDatabase();

    $.ajax({
        url: 'api/auth/logout',
        method: 'POST',
        Accept: 'application/json',
        contentType: 'application/json'
    }).done(function () {
        console.log('IŠSILOGINTA');

        delete window.localStorage.token;

        $('#mar-adminButton').hide();
        $('#mar-signOutButton').hide();
        $('#mar-signInButton').show();
        $('#mar-loggedUserName').text("User: Guest");

    }).fail(function () {
        console.log('NE IŠSILOGINTA')
    });
}

function synchronizeCarts(username) {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/cart/synchronize',
        method: 'PUT',
        Accept: 'application/json',
        dataType: 'json',
        headers: {Authorization: "Bearer " + token}
    }).done(function () {

        console.log("GUEST KREPŠELIS SINCHRONIZUOTAS SU:\n " + username);
        // console.log("User cart=" + userCart);

    }).fail(function () {
        alert("KREPŠELIS NESINCHRONIZUOTAS");
    });
}

function keepUserCartInDatabase() {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/cart/keepusercart',
        method: 'PUT',
        Accept: 'application/json',
        dataType: 'json',
        headers: {Authorization: "Bearer " + token}
    }).done(function () {
        console.log("VARTOTOJO KREPŠELIS ISSAUGOTAS DB");
    }).fail(function () {
        alert("VARTOTOJO KREPŠELIS NE ISSAUGOTAS DB");
    });
}

function buy() {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/order/buy',
        method: 'POST',
        dataType: 'json',
        Accept: 'application/json',
        contentType: 'application/json',
        headers: {Authorization: "Bearer " + token},
        data: JSON.stringify({
            // username: "ok",
            // password: "ok"
        })
    }).done(function (order) {
        console.log('APMOKĖTA');
        console.log('orderLinesSize=' + order.orderLines.length);
        printOrder(order);

    }).fail(function () {
        console.log('NE APMOKĖTA');
        alert('NE APMOKĖTA!');
    });
}

// ORDER LIST ----------------------------------------------------------------------------------------------------------
function printOrderList() {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/order/getorderlist',
        method: 'GET',
        dataType: 'json',
        headers: {Authorization: "Bearer " + token}
    }).done(function (orderList) {
        console.log("ORDER LIST ATSPAUSDINTA");
        bildHtmlOrderListRows(orderList);
    }).fail(function () {
        console.log("ORDER LIST NEATSPAUSDINTA");
    });
}

function bildHtmlOrderListRows(orderList) {

    var html = '';
    for (var i = 0; i < orderList.length; i++) {

        html += addHtmlOrderListRow(orderList[i]);
    }

    $('#mar-orderListViewTbody').html(html);
    $('#mar-orderListModalView').modal('show');
}

function addHtmlOrderListRow(order) {

    console.log("order.user=" + order.user.username);
    var html = '';
    html += '<tr>';
    html += ' <td data-th="Id"><h5 class="nomargin">' + order.id + '</h5></td>';
    html += ' <td data-th="Date">' + order.date.toLocaleString() + '</td>';
    html += ' <td data-th="User">' + order.user.username + '</td>';
    html += ' <td data-th="Total" class="text-right">€ ' + order.total.toLocaleString() + '</td>';
    html += ' <td data-th="" class="text-right">';
    html += '  <a href="#" class="nav-link btn btn-info btn-sm" onclick="printOrder(' + order.id + ')">';
    html += '   <span class="glyphicon glyphicon-shopping-cart"></span> Show</a>';
    html += ' </td>';
    html += '</tr>';
    return html;
}

// ORDER ---------------------------------------------------------------------------------------------------------------
function printOrder(orderId) {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/order/getorder/' + orderId,
        method: 'GET',
        dataType: 'json',
        headers: {Authorization: "Bearer " + token}
    }).done(function (order) {
        console.log("ORDER ATSPAUSDINTA");
        bildHtmlOrderRows(order);
    }).fail(function () {
        console.log("ORDER NEATSPAUSDINTA");
    });
}

function bildHtmlOrderRows(order) {

    var orderLines = order.orderLines;

    var html = '';
    for (var i = 0; i < orderLines.length; i++) {

        html += addHtmlOrderRow(orderLines[i].id, orderLines[i].qty, orderLines[i].product);
    }

    $('#mar-orderViewTbody').html(html);
    $('#mar-orderUserName').html('Order Qwner: user ' + order.user.username);
    $('#mar-orderDate').html(order.date.toLocaleString());
    $('#mar-orderTotal').html(order.total.toLocaleString());
    $('#mar-orderModalView').modal('show');
}

function addHtmlOrderRow(cartLineId, cartLineQty, product) {

    var html = '<tr>';
    html += '<td data-th="Product"><h5 class="nomargin">' + product.name + '</h5></td>';
    html += '<td data-th="Price" class="text-right">€ ' + product.price.toLocaleString() + '</td>';
    html += '<td data-th="Quantity" class="text-right">' + cartLineQty + '</td>';
    html += '<td data-th="Subtotal" class="text-right">€ ' + (product.price * cartLineQty).toLocaleString() + '</td>';
    html += '</tr>';
    return html;
}


