const initialState = {
    authUser: null,
    categories: [],
    cart: null,
    page: 'home',
};

const AppContext = React.createContext(null);

function reducer( state, action ) {
    switch( action.type ) {
        case 'authenticate' :
            // if( ! window.localStorage.getItem( "auth-user" ) ) {
                window.localStorage.setItem("auth-user", JSON.stringify(action.payload));
            // }
            return { ...state,
                authUser: action.payload,
            };
        case 'cart':
            return { ...state,
                cart: action.payload,
            };
        case 'categories':
            return { ...state,
                categories: action.payload,
            };
        case 'logout' :
            window.localStorage.removeItem( "auth-user" );
            return { ...state,
                authUser: null,
                cart: null
            };
        case 'navigate':
            window.location.hash = action.payload;
            return { ...state,
                page: action.payload,
            };
    }
}

function App({contextPath, homePath}) {
    const [state, dispatch] = React.useReducer( reducer, initialState );
    const loadCategories = React.useCallback( () => {
        fetch(`${contextPath}/shop/category`)
            .then(r => r.json())
            .then(j => dispatch({type: 'categories', payload: j.data}));
    });
    const checkHash = React.useCallback( () => {
        let hash = window.location.hash;
        let path = ( hash.length > 1 ) ? hash.substring(1) : "home";
        dispatch( { type: "navigate", payload: path } );
    } ) ;
    const request = React.useCallback( (url, params) => new Promise( (resolve, reject) => {
        if( url.startsWith('/') ) {
            url = contextPath + url;
        }
        if( state.authUser && state.authUser.token && state.authUser.token.tokenId ) {
            if( typeof params === 'undefined' || params == null ) {
                params = {};
            }
            if( typeof params.headers === 'undefined' ) {
                params.headers = {};
            }
            if( typeof params.headers.Authorization === 'undefined' ) {
                params.headers.Authorization = "Bearer " + state.authUser.token.tokenId;
            }
        }
        fetch( url, params )
            .then(r => r.json())
            .then(j => {
                if (j.status.isSuccessful) { resolve( j.data ); }
                else { reject( j.data ); }
            });
    } ) );
    const refreshCart = React.useCallback( () => {
        request('/shop/cart')
            .then(cart => dispatch({type: "cart", payload: cart}))
            .catch( ()=>{} );
    } );
    React.useEffect( () => {
        let authUser = window.localStorage.getItem( "auth-user" );
        if( authUser ) {
            authUser = JSON.parse( authUser );
            let token = authUser.token;
            if( token ) {
                let exp = new Date(token.exp);
                if( exp < new Date() ) {
                    dispatch({type: 'logout'});
                }
                else {
                    dispatch({type: 'authenticate', payload: authUser});
                }
            }
        }
        checkHash();
        window.addEventListener('hashchange', checkHash);
        loadCategories();
        return () => {
            window.removeEventListener('hashchange', checkHash);
        };
    }, [] );
    React.useEffect( () => {
        refreshCart();
    }, [state.authUser] );

    return <AppContext.Provider value={{state, dispatch, contextPath, loadCategories, request, refreshCart}}>
        <header>
            <nav className="navbar navbar-expand-lg bg-body-tertiary">
                <div className="container-fluid">
                    <a className="navbar-brand"
                       onClick={() => dispatch({type: "navigate", payload: "home"})}>Крамниця</a>
                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse"
                            data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                            aria-expanded="false" aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarSupportedContent">
                        <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                            <li className="nav-item">
                                <a className="nav-link"
                                   onClick={() => dispatch({type: "navigate", payload: "home"})}>Домашня</a>
                            </li>
                            <li className="nav-item">
                                <a className="nav-link "
                                   onClick={() => dispatch({type: "navigate", payload: "cart"})}>Кошик
                                    <span className="cart-widget-quantity">{
                                    (state.cart && state.cart.cartItems && state.cart.cartItems.length > 0)
                                        ? state.cart.cartItems.reduce((s,c)=>s+c.quantity, 0)
                                        : 0
                                    }</span></a>
                            </li>

                        </ul>
                        <form className="d-flex nav-search" role="search">
                            <input className="form-control me-2" type="search" placeholder="Search"
                                   aria-label="Search"/>
                            <button className="btn btn-outline-success" type="submit"><i className="bi bi-search"></i>
                            </button>
                        </form>

                        {!state.authUser && <div>
                            <button type="button" className="btn btn-outline-secondary"
                                    data-bs-toggle="modal" data-bs-target="#authModal">
                                <i className="bi bi-box-arrow-in-right"></i>
                            </button>
                            <button type="button" className="btn btn-outline-secondary"
                                    onClick={() => dispatch({type: 'navigate', payload: 'signup'})}>
                                <i className="bi bi-person-add"></i>
                            </button>
                        </div>}

                        {state.authUser && <div>
                            <img src={"storage/" + state.authUser.avatarUrl}
                                 alt={state.authUser.userName}
                                 className="nav-avatar"/>

                            <button type="button" className="btn btn-outline-warning"
                                    onClick={() => dispatch({type: 'logout'}) }>
                                <i className="bi bi-box-arrow-right"></i>
                            </button>
                            {state.authUser.role.canCreate &&
                                <button type="button" className="btn btn-outline-warning"
                                        onClick={() => dispatch({type: 'navigate', payload: 'admin'})}>
                                    <i className="bi bi-speedometer2"></i>
                                </button>
                            }
                        </div>}

                    </div>
                </div>
            </nav>
        </header>
        <main className="container">
            { state.page === 'admin'  && <Admin/>  }
            { state.page === 'cart'   && <Cart/>   }
            { state.page === 'home'   && <Home/>   }
            { state.page === 'signup' && <Signup/> }
            { state.page.startsWith('category/') && <Category id={state.page.substring(9)}/> }
            { state.page.startsWith('product/') && <Product id={state.page.substring(8)}/> }
        </main>
        <div className="spacer"></div>

       <AuthModal />

        <footer className="bg-body-tertiary px-3 py-2">
            &copy; 2024, ITSTEP KN-P-213
        </footer>
    </AppContext.Provider>;
}

function Admin() {
    const {state, dispatch, contextPath, loadCategories} = React.useContext(AppContext);
    React.useEffect( () => {
        if(!state.authUser || !state.authUser.role || !state.authUser.role.canCreate) {
            dispatch({type: 'navigate', payload: 'home'});
        }
    }, [] );
    const categoryFormRef = React.useRef();
    const productFormRef = React.useRef();
    const onCategorySubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch(`${contextPath}/shop/category`, {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Категорія успішно створена");
                categoryFormRef.current.reset();
                loadCategories();
            } else {
                alert(j.data);
            }
        });
    });
    const onProductSubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch(`${contextPath}/shop/product`, {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Товар успішно створений");
                productFormRef.current.reset();
            } else {
                alert(j.data);
            }
        });
    });

    return <div>
        <h1>Панель адміністрування</h1>
        <hr/>
        <h2>Створення товарних категорій</h2>
        <form encType="multipart/form-data" method="POST"
              onSubmit={onCategorySubmit} ref={categoryFormRef}>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="name-addon"><i className="bi bi-info-square"></i></span>
                        <input type="text" className="form-control"
                               name="category-name" placeholder="Назва"
                               aria-label="Назва" aria-describedby="name-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="description-addon"><i
                            className="bi bi-card-text"></i></span>
                        <input type="text" className="form-control"
                               name="category-description" placeholder="Опис"
                               aria-label="Опис" aria-describedby="description-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <label className="input-group-text" htmlFor="category-image"><i
                            className="bi bi-card-image"></i></label>
                        <input type="file" className="form-control" name="category-image" id="category-image"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="slug-addon"><i className="bi bi-link"></i></span>
                        <input type="text" className="form-control"
                               name="category-slug" placeholder="Slug"
                               aria-label="Slug" aria-describedby="slug-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">

                </div>
                <div className="col col-6">
                    <button type="submit" className="btn btn-outline-success">Створити</button>
                </div>
            </div>
        </form>
        <hr/>
        <h2>Створення товарів</h2>
        <form encType="multipart/form-data" method="POST"
              onSubmit={onProductSubmit} ref={productFormRef}>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-name-addon"><i className="bi bi-info-square"></i></span>
                        <input type="text" className="form-control"
                               name="product-name" placeholder="Назва"
                               aria-label="Назва" aria-describedby="product-name-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-description-addon"><i
                            className="bi bi-card-text"></i></span>
                        <input type="text" className="form-control"
                               name="product-description" placeholder="Опис"
                               aria-label="Опис" aria-describedby="product-description-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <label className="input-group-text" htmlFor="product-image"><i
                            className="bi bi-card-image"></i></label>
                        <input type="file" className="form-control" name="product-image" id="product-image"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-slug-addon"><i className="bi bi-link"></i></span>
                        <input type="text" className="form-control"
                               name="product-slug" placeholder="Slug"
                               aria-label="Slug" aria-describedby="product-slug-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-price-addon"><i
                            className="bi bi-cash"></i></span>
                        <input type="number" step="0.01" className="form-control"
                               name="product-price" placeholder="Ціна"
                               aria-label="Ціна" aria-describedby="product-price-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <select name="category-id" className="form-select" aria-label="Вибір категорії">
                        {state.categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="product-quantity-addon"><i
                            className="bi bi-1-circle"></i></span>
                        <input type="number" step="1" className="form-control" defaultValue="1"
                               name="product-quantity" placeholder="Кількість"
                               aria-label="Кількість" aria-describedby="product-quantity-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <button type="submit" className="btn btn-outline-success">Створити</button>
                </div>
            </div>
        </form>
        <hr/>
    </div>;
}

function Signup() {
    const {contextPath} = React.useContext(AppContext);
    const formRef = React.useRef();
    const onFormSubmit = React.useCallback(e => {
        e.preventDefault();
        const formData = new FormData(e.target);
        fetch(`${contextPath}/auth`, {
            method: "POST",
            body: formData
        }).then(r => r.json()).then(j => {
            if (j.status.isSuccessful) {
                alert("Ви успішно зареєстровані");
                formRef.current.reset();
            } else {
                alert(j.data);
            }
        });
    });
    return <div>
        <h1>Реєстрація нового користувача</h1>

        <form encType="multipart/form-data" method="POST"
              onSubmit={onFormSubmit} ref={formRef}>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="name-addon"><i className="bi bi-person-badge"></i></span>
                        <input type="text" className="form-control"
                               name="signup-name" placeholder="Ім'я"
                               aria-label="Ім'я" aria-describedby="name-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="birthdate-addon"><i className="bi bi-cake"></i></span>
                        <input type="date" className="form-control"
                               name="signup-birthdate" placeholder="Дата народження"
                               aria-label="Дата народження" aria-describedby="birthdate-addon"/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="phone-addon"><i className="bi bi-phone"></i></span>
                        <input type="text" className="form-control"
                               name="signup-phone" placeholder="Телефон"
                               aria-label="Телефон" aria-describedby="phone-addon"/>
                    </div>
                </div>
                <div className="col col-6">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="email-addon"><i className="bi bi-envelope-at"></i></span>
                        <input type="text" className="form-control"
                           name="signup-email" placeholder="Ел. пошта"
                           aria-label="Ел. пошта" aria-describedby="email-addon"/>
                </div>
            </div>
        </div>
        <div className="row">
            <div className="col col-6">
                <div className="input-group mb-3">
                    <span className="input-group-text" id="login-addon"><i
                        className="bi bi-box-arrow-in-right"></i></span>
                    <input type="text" className="form-control"
                           name="signup-login" placeholder="Логін"
                           aria-label="Логін" aria-describedby="login-addon"/>
                </div>
            </div>
            <div className="col col-6">
                <div className="input-group mb-3">
                    <label className="input-group-text" htmlFor="signup-avatar"><i className="bi bi-person-circle"></i></label>
                    <input type="file" className="form-control" name="signup-avatar" id="signup-avatar"/>
                </div>
            </div>
        </div>
        <div className="row">
            <div className="col col-6">
                <div className="input-group mb-3">
                    <span className="input-group-text" id="password-addon"><i className="bi bi-lock"></i></span>
                    <input type="text" className="form-control"
                           name="signup-password" placeholder="Вигадайте пароль"
                           aria-label="Вигадайте пароль" aria-describedby="password-addon"/>
                </div>
            </div>
            <div className="col col-6">
                <div className="input-group mb-3">
                    <span className="input-group-text" id="repeat-addon"><i className="bi bi-unlock"></i></span>
                    <input type="text" className="form-control"
                           name="signup-repeat" placeholder="Повторіть пароль"
                           aria-label="Повторіть пароль" aria-describedby="repeat-addon"/>
                </div>
            </div>
        </div>
        <div className="row">
            <button type="submit" className="btn btn-outline-success">Реєстрація</button>
        </div>
        </form>
    </div>
}

function AuthModal() {
    const {contextPath, dispatch} = React.useContext(AppContext);
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const authModalRef = React.useRef();
    const authClick = React.useCallback(() => {
        // console.log(login, password);
        fetch(`${contextPath}/auth`, {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + btoa(login + ':' + password)
            }
        }).then(r => r.json()).then(j => {
            // console.log(j);
            if (j.status.isSuccessful) {
                // j.data - дані про користувача, токен та права (роль)
                // задача: зберегти ці дані і використовувати без повторної автентифікації
                // куди можна зберігати? а) state/context б) sessionStorage в) localStorage
                dispatch({type: 'authenticate', payload: j.data});
                bootstrap.Modal.getInstance(authModalRef.current).hide();
            } else {
                alert(j.data);
            }
        });
    });
    return <div className="modal fade" id="authModal" tabIndex="-1" ref={authModalRef}
                aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div className="modal-dialog">
            <div className="modal-content">
                <div className="modal-header">
                    <h1 className="modal-title fs-5" id="exampleModalLabel">Вхід до системи</h1>
                    <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div className="modal-body">
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="login-addon"><i
                            className="bi bi-person-fill-lock"></i></span>
                        <input type="text" className="form-control" placeholder="Логін" aria-label="Логін"
                               onChange={e => setLogin(e.target.value)}
                               aria-describedby="login-addon"/>
                    </div>
                    <div className="input-group mb-3">
                        <span className="input-group-text" id="password-addon"><i className="bi bi-key-fill"></i></span>
                        <input type="password" className="form-control" placeholder="******" aria-label="Пароль"
                               onChange={e => setPassword(e.target.value)}
                               aria-describedby="password-addon"/>
                    </div>
                </div>
                <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Скасувати</button>
                    <button type="button" className="btn btn-primary" onClick={authClick}>Вхід</button>
                </div>
            </div>
        </div>
    </div>;
}

function Cart() {
    const {state, dispatch, request, refreshCart} = React.useContext(AppContext);
    React.useEffect( () => {
        //refreshCart();
    }, [state.authUser]);
    const incCartItem = React.useCallback( (item, delta) => {
        if( Number(item.quantity) + Number(delta) === 0 ) {
            if( ! confirm("Видалити товар з кошику?") ) {
                return;
            }
        }
        request(`/shop/cart?cart-id=${item.cartId}&product-id=${item.productId}&delta=${delta}`, {
            method: 'PUT',
        }).then(refreshCart)
            .catch(alert);
    });
    const delCartItem = React.useCallback( (item) => {
        if( ! confirm("Видалити товар з кошику?") ) {
            return;
        }
        request(`/shop/cart?cart-id=${item.cartId}&product-id=${item.productId}`, {
            method: 'DELETE',
        }).then(refreshCart)
            .catch(alert);
    });
    const delCart = React.useCallback( () => {
        if( ! confirm("Скасувати весь кошик?") ) {
            return;
        }
        request(`/shop/cart?cart-id=${state.cart.id}`, {
            method: 'DELETE',
        }).then(refreshCart)
            .catch(alert);
    });
    const buyCart = React.useCallback( () => {
        if( ! confirm(`Підтверджуєте покупку на суму ${
                state.cart.cartItems.reduce((s,c)=>s+c.price, 0.0).toFixed(2)} грн?`) ) {
            return;
        }
        // request(`/shop/cart?cart-id=${state.cart.id}`, {
        //     method: 'DELETE',
        // }).then(refreshCart)
        //     .catch(alert);
    });
    return <div>
        <h2>Кошик</h2>
        {(state.cart && state.cart.cartItems)
        ? <React.Fragment>
            {state.cart.cartItems.map(item => <div className="row cart-row align-items-center" key={item.productId}>
                    <div className="col col-1">
                        <picture onClick={()=>dispatch({type: 'navigate',
                                payload: 'product/' + (item.product.slug || item.product.id)})}>
                            <img src={"storage/" + item.product.imageUrl} alt="product"/>
                        </picture>
                    </div>
                    <div className="col col-4">
                        {item.product.name}
                    </div>
                    <div className="col col-1">
                        {item.product.price.toFixed(2)}
                    </div>
                    <div className="col col-1">
                        {item.quantity}
                    </div>
                    <div className="col col-2">
                        {item.price.toFixed(2)}
                    </div>
                    <div className="col col-3">
                        <button onClick={() => incCartItem(item, -1)} className="btn btn-outline-warning"><i className="bi bi-bag-dash"></i></button>
                        <button onClick={() => incCartItem(item, 1) } className="btn btn-outline-success"><i className="bi bi-bag-plus"></i></button>
                        <button onClick={() => delCartItem(item)    } className="btn btn-outline-danger"><i className="bi bi-bag-x"></i></button>
                    </div>
                </div>)}
                {state.cart.cartItems.length > 0 && <div className="row">
                    <div className="col offset-5 col-1">
                        Разом
                    </div>
                    <div className="col col-1 align-self-center">
                        {state.cart.cartItems.reduce((s,c)=>s+c.quantity, 0)}
                    </div>
                    <div className="col col-2">
                        {state.cart.cartItems.reduce((s,c)=>s+c.price, 0.0).toFixed(2)}
                    </div>
                    <div className="col">
                        <button onClick={buyCart} className="btn btn-success" title="Придбати"><i className="bi bi-basket3"></i></button>
                        <button onClick={delCart} className="btn btn-danger" title="Скасувати"><i className="bi bi-trash3"></i></button>
                    </div>
                </div>}
            </React.Fragment>
            : <h3>Кошик порожній</h3>}
    </div>;
}

function Home() {
    const {state, dispatch} = React.useContext(AppContext);
    return <div>
        <h2>Домашня</h2>
        {state.categories.map(c => <div
            key={c.id} className="home-category"
            onClick={() => dispatch({type: 'navigate', payload: 'category/' + (c.slug || c.id) })}>
            <h3>{c.name}</h3>
            <picture>
                <img src={"storage/" + c.imageUrl} alt="category" />
            </picture>
            <p>{c.description}</p>
        </div>)}
    </div>;
}

function Category({id}) {
    const { contextPath } = React.useContext(AppContext);
    const [products, setProducts] = React.useState([]);
    React.useEffect( () => {
        fetch(`${contextPath}/shop/product?category=${id}`)
            .then(r => r.json())
            .then(j => {
                if(j.status.isSuccessful) {
                    setProducts(j.data);
                }
                else {
                    console.error(j.data);
                }
            });
    }, [id]);
    return <div>
        <h2>Category page: {id}</h2>
        {products.map(p => <ProductCard p={p} key={p.id}/>)}
    </div>;
}

function Product({id}) {
    const {request, dispatch, refreshCart} = React.useContext(AppContext);
    const [product, setProduct] = React.useState({});
    React.useEffect( () => {
        request('/shop/product?id=' + id)
            .then( setProduct )
            .catch( console.error );
    }, [id] );
    const cartClick = React.useCallback( () => {
        request('/shop/cart?product-id=' + product.id, {
            method: 'POST'
        }).then(refreshCart).catch(alert);
    });
    return <div>
        <h2>Сторінка товару</h2>
        {product.id && <div>
            <div className="row">
                <div className="col col-5">
                    <div className="product-page-left">
                        <picture>
                            <img src={"storage/" + product.imageUrl} alt="product"/>
                        </picture>
                    </div>
                </div>
                <div className="col col-7">
                    <h3>{product.name}</h3>
                    <p>{product.description}</p>
                    <h4>{product.price.toFixed(2)}</h4>
                    <i>Перевірити на наявність у кошику, змінити надпис на кнопці</i>
                    <button onClick={e => {e.stopPropagation();cartClick();} }>До кошику</button>
                    <hr/>
                    <h5>Вас також може зацікавити:</h5>
                    {product.similarProducts && product.similarProducts.map(p =>
                        <ProductCard p={p} isSmall={true} key={p.id} /> )}
                </div>
            </div>
        </div>
        }{!product.id && <div>
        Не знайдено
    </div>}
    </div>;
}

function ProductCard({p, isSmall}) {
    const {state, dispatch, request, refreshCart} = React.useContext(AppContext);
    const cartPost = React.useCallback( (e, product) => {
        e.stopPropagation();
        request('/shop/cart?product-id=' + product.id, {
            method: 'POST'
        }).then(refreshCart).catch(alert);
    });
    const cartPut = React.useCallback( (e, product) => {
        e.stopPropagation();
        request(`/shop/cart?cart-id=${state.cart.id}&product-id=${product.id}&delta=1`, {
            method: 'PUT',
        }).then(refreshCart)
            .catch(alert);
    });
    return <div key={p.id} className={"product-card " + (isSmall ? "scale-75" : "") }
                onClick={() => dispatch({type: 'navigate', payload: 'product/' + (p.slug || p.id)})}>
        <picture>
            <img src={"storage/" + p.imageUrl} alt="product"/>
        </picture>
        <h3>{p.name}</h3>
        <p>{p.description}</p>
        <h4>₴ {p.price.toFixed(2)}</h4>
        {(state.cart && state.cart.cartItems && state.cart.cartItems.some(ci => ci.productId === p.id))
            ? <span className="cart-fab" onClick={(e) => cartPut(e, p)}><i className="bi bi-plus-circle"></i></span>
            : <span className="cart-fab" onClick={(e) => cartPost(e, p)}><i className="bi bi-bag-check"></i></span>
        }
    </div>
}


const domRoot = document.getElementById("app-container");
const cp = domRoot.getAttribute("data-context-path");
const hp = domRoot.getAttribute("data-home-path");
// console.log(cp);
ReactDOM
    .createRoot(domRoot)
    .render(<App contextPath={cp} homePath={hp}/>);
/*
Д.З. Впровадити видалення замовленого контенту у
власний курсовий проєкт. (кошик/білети/послуги)
 */