const initialState = {
    authUser: null,
    page: 'home',
    categories: [],
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
        case 'categories':
            return { ...state,
                categories: action.payload,
            };
        case 'logout' :
            window.localStorage.removeItem( "auth-user" );
            return { ...state,
                authUser: null,
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

    return <AppContext.Provider value={{state, dispatch, contextPath, loadCategories}}>
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
                                   onClick={() => dispatch({type: "navigate", payload: "cart"})}>Кошик</a>
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
        console.log(login, password);
        fetch(`${contextPath}/auth`, {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + btoa(login + ':' + password)
            }
        }).then(r => r.json()).then(j => {
            console.log(j);
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
        const {state, dispatch} = React.useContext(AppContext);
        return <div>
        <h2>Кошик</h2>
        <b onClick={() => dispatch({type: "navigate", payload: "home"})}>На Домашню</b>
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
    const { contextPath, dispatch } = React.useContext(AppContext);
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
    const cartClick = React.useCallback( e => {
        e.stopPropagation();
    });
    return <div>
        <h2>Category page: {id}</h2>
        {products.map(p => <div key={p.id} className="product-card"
            onClick={() => dispatch({type: 'navigate', payload: 'product/' + (p.slug || p.id) })}>
            <picture>
                <img src={"storage/" + p.imageUrl} alt="product" />
            </picture>
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <h4>₴ {p.price.toFixed(2)}</h4>
            <span className="cart-fab" onClick={cartClick}><i className="bi bi-bag-check"></i></span>
        </div>)}
    </div>;
}

function Product({id}) {
    return <div>
        <h2>Product page: {id}</h2>
    </div>;
}


const domRoot = document.getElementById("app-container");
const cp = domRoot.getAttribute("data-context-path");
const hp = domRoot.getAttribute("data-home-path");
// console.log(cp);
ReactDOM
    .createRoot(domRoot)
    .render(<App contextPath={cp} homePath={hp}/>);
/*
Д.З. Впровадити слагіфікацію основного контенту у
власний курсовий проєкт.
 */