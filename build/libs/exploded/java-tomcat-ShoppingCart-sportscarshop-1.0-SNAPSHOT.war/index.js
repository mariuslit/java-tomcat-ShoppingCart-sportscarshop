$(function () {

    var skip = 0;
    var rowsPerPage = 3;

    printProducts(skip, rowsPerPage);

    $('.mar-invisible').hide();
    $('#mar-signOut').hide();
    $('#mar-cartView').hide();

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
        keepUserCartInDatabase()
    });

    // [Sign In]
    $('#mar-signIn').click(function () {
        $('#mar-loginModal').modal('show');
    });

    // [Sign In]
    $('#mar-loginButton').click(function () {
        login($('#username').val(), $('#password').val(), skip, rowsPerPage);
    });

    // [Sign Out]
    $('#mar-signOut').click(function () {
        logout();
    });
});

// PRODUCTS ------------------------------------------------------------------------------------------------------------
function printProducts(skip, rowsPerPage) {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/product/list',
        method: 'GET',
        headers: {
            Authorization: "Bearer " + token
        },
        dataType: 'json',
        data: {
            size: rowsPerPage + 1,
            skip: skip
        }
    }).done(function (data) {

        // << >> show/hide
        // if (skip === 0) $('#prev').hide();
        // else $('#prev').show();
        if (data.length <= rowsPerPage) $('#next').hide();
        else $('#next').show();

        bildHtmlProductsRows(data, rowsPerPage);

    }).fail(function (jrXHR) {

        // kartoti autentifikavimą
        if (jrXHR.status === 401) {

            console.log("fail Product List error " + jrXHR.status);

            $('#mar-loginButton').click(function () {
                login($('#username').val(), $('#password').val(), skip, rowsPerPage);
            });

            $('#mar-loginModal').modal('show');

        } else {
            console.log("fail Product List eror " + jrXHR.status);
        }
    });
}

function bildHtmlProductsRows(products, rowsPerPage) {

    var html = '';

    for (var i = 0; i < Math.min(products.length, rowsPerPage); i++) {

        var productId = products[i].id;
        var productName = products[i].name;
        var productPrice = products[i].price.toLocaleString();
        var productImage = products[i].image;

        html += '<tr class="ml-product">';
        html += ' <td class="text-right"><img src="' + productImage + '" alt="Responsive image" class="img-fluid" /></td>';
        html += ' <td>' + productName + '</td>';
        html += ' <td class="text-right">' + productPrice + '</td>';
        html += ' <td class="text-right">';
        html += '  <a href="#" class="nav-link btn btn-info btn-sm ml-add-krepselisX" ' +
            'onclick="jamam(' + productId + ', \'' + productName + '\')">';
        html += '   <span class="glyphicon glyphicon-shopping-cart"></span> Add to Cart</a>';
        html += ' </td>';
        html += '</tr>';
    }
    $('#mar-productListTbody').html(html);
}

// CART ----------------------------------------------------------------------------------------------------------------
function jamam(productId, productName) {

    // console.log("paspausta prekė " + productId);

    // 1 idedama preke i krepseli
    $.ajax({
        url: 'api/cart/add', // dedama į 1 krepseli
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({
            id: productId,
            qty: 1
        })
    }).done(function () {
        console.log("prekė " + productId + " įdėta į krepšį");

        // 2 atspausdinamos visos krepselio prekes
        printCart();
        // alert('Prekė įdėta į krepšelį:\n    product.name=' + productName);

    }).fail(function () {

        console.log('neįdėta į krepšelį');
    });
}

function printCart() {
    console.log('spausdinamos cart prekės');
    // console.log('skip=' + skip + ' row=' + rowsPerPage);

    var token = window.localStorage.token;

    // gauna cart produktų sarašą
    $.ajax({
        url: 'api/cart',
        method: 'GET',
        dataType: 'json',
        headers: {
            Authorization: "Bearer " + token
        }
    }).done(function (cart) {
        // console.log("ajax data.lenght=" + cart.cartLines.length);

        bildHtmlCartRows(cart);

    }).fail(function () {

        console.log("fail Product List error");
    });
}

function bildHtmlCartRows(cart) {
    // console.log("bildHtmlCartRows");

    var cartLines = cart.cartLines;

    console.log("  cartLines.lenght=" + cartLines.length);

    var html = '';
    for (var i = 0; i < cartLines.length; i++) {

        var cartLineId = cartLines[i].id;
        var cartLineQty = cartLines[i].qty;
        var product = cartLines[i].product;

        // console.log('  cartLineId=' + cartLineId + ', cartLineQty=' + cartLineQty + ', product.name=' + product.name);

        html += addHtmlCartRow(cartLineId, cartLineQty, product);
    }
    $('#mar-cartListTbody').html(html);
    $('#mar-totalSum').html(cart.totalSum.toLocaleString());
    $('#mar-totalSumRespons').html(cart.totalSum.toLocaleString());
    $('.mar-invisible').hide();

    $('.mar-refreshCartLine').on('click', function () {

        var oldQty = Number($(this).closest('tr').find('input')[0].value);
        var productId = Number($(this).closest('tr').find('td')[0].innerHTML);

        console.log('productId=' + productId);
        console.log('oldQty=' + oldQty);

        updateCartLine(productId, oldQty);

    });
}

function addHtmlCartRow(cartLineId, cartLineQty, product) {

    var html = '<tr>';
    // html += ' <td data-th=""></td>';
    html += ' <td class="mar-invisible mar-cartLineProductId">' + product.id + '</td>';
    html += ' <td data-th="Product">';
    html += '   <div class="row">';
    // <img src="http://placehold.it/1" alt="..." class="img-responsive"/>
    html += '     <div class="col-sm-2 hidden-xs"><img src="' + product.image + '"  alt="..." class="img-fluid"/></div>'; // picture place
    html += '     <div class="col-sm-10">';
    html += '       <h4 class="nomargin">' + product.name + '</h4>';
    html += '       <p>Good car!' + '</p>'; // dscription place
    html += '     </div>';
    html += '   </div>';
    html += ' </td>';
    html += ' <td data-th="Price" class="text-right">€ ' + product.price.toLocaleString() + '</td>';
    html += ' <td data-th="Quantity" class="text-right"><input type="number" class="form-control" value="' + cartLineQty + '"></td>';
    html += ' <td data-th="Subtotal" class="text-right">€ ' + (product.price * cartLineQty).toLocaleString() + '</td>';
    html += ' <td class="actions text-right" data-th="">';
    html += '   <button class="btn btn-info btn-sm mar-refreshCartLine"><i class="fa fa-refresh"></i></button>';
    // html += '   <button class="btn btn-info btn-sm mar-refreshCartLine" ' + 'onclick="updateCartLine2(' + product.id + ', \'' + cartLineId + '\')"><i class="fa fa-refresh"></i></button>';
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

        console.log("produktas: " + productId + " atnaujintas su PUT")
    });
    printCart()
}

// DELETE --------------------------------------------------------------------------------------------------------------
function deleteCartLine(productId) {
    $.ajax({
        url: 'api/cart/deleteCartLine/' + productId,
        method: 'DELETE',
        dataType: 'json',
        contentType: 'application/json',
        data: {
            // id: productId
        }
    }).done(function () {
        console.log("produktas id=" + productId + " istrintas.")

    });
    printCart()
}

// LOGIN ---------------------------------------------------------------------------------------------------------------
// var token = window.localStorage.token;
// var session = window.sessionStorage;

function login(username, password, skip, rowsPerPage) {

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

        sinchronizuotiKrepselius();

        window.localStorage.token = data.token;

        $('#mar-signIn').hide();
        $('#mar-signOut').show();
        $('#mar-loginModal').modal('hide');
        $('#mar-loggedUserName').text('User: ' + username);

    }).fail(function () {
        $('#mar-loginModal').modal('hide');
        console.log('NE PRISILOGINTA');
        alert('Neprisijungta, neteisingi duomenys!');
    });
}

function sinchronizuotiKrepselius() {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/mar/cart-cart',
        method: 'PUT',
        Accept: 'application/json',
        dataType: 'json',
        headers: {Authorization: "Bearer " + token}
    }).done(function (userCart) {
        console.log("KREPŠELIS SINCHRONIZUOTAS");
        console.log("User cart=" + userCart);

    }).fail(function () {
        console.log("KREPŠELIS NESINCHRONIZUOTAS");
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

        $('#mar-signOut').hide();
        $('#mar-signIn').show();
        $('#mar-loggedUserName').text("User: Guest");

    }).fail(function () {
        console.log('NE IŠSILOGINTA')
    });
}

function keepUserCartInDatabase() {

    var token = window.localStorage.token;

    $.ajax({
        url: 'api/mar/keepusercart',
        method: 'PUT',
        Accept: 'application/json',
        dataType: 'json',
        headers: {Authorization: "Bearer " + token}
    }).done(function (userCart) {
        alert("VARTOTOJO KREPŠELIS ISSAUGOTAS DB");
        console.log("User cart=" + userCart);

    }).fail(function () {
        alert("VARTOTOJO KREPŠELIS NE ISSAUGOTAS DB");
    });
}

