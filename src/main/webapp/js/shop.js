const initialState = {
    auth: {
        token: null,
        user: null
    },
    page: 'home',
};

const AppContext = React.createContext(null);

function reducer( state, action ) {
    switch( action.type ) {
        case 'navigate':
            window.location.hash = action.payload;
            return { ...state,
                page: action.payload,
            };
    }
}

function App({contextPath, homePath}) {
    const [state, dispatch] = React.useReducer( reducer, initialState );
    React.useEffect( () => {
        let hash = window.location.hash;
        if( hash.length > 1 ) {
            dispatch( { type: "navigate", payload: hash.substring(1) } );
        }
    }, [] );
    return <AppContext.Provider value={{state, dispatch}}>
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

                        <button type="button" className="btn btn-outline-secondary"
                                data-bs-toggle="modal" data-bs-target="#authModal">
                            <i className="bi bi-box-arrow-in-right"></i>
                        </button>
                    </div>
                </div>
            </nav>
        </header>
        <main className="container">
            {state.page === 'home' && <Home/>}
            {state.page === 'cart' && <Cart/>}
        </main>
        <div className="spacer"></div>


        <div className="modal fade" id="authModal" tabIndex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
            <AuthModal />
        </div>

        <footer className="bg-body-tertiary px-3 py-2">
            &copy; 2024, ITSTEP KN-P-213
        </footer>
    </AppContext.Provider>;
}

function AuthModal() {
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const authClick = React.useCallback( () => {
        console.log(login, password);
    });
    return <div className="modal-dialog">
        <div className="modal-content">
            <div className="modal-header">
                <h1 className="modal-title fs-5" id="exampleModalLabel">Вхід до системи</h1>
                <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div className="modal-body">
                <div className="input-group mb-3">
                    <span className="input-group-text" id="login-addon"><i className="bi bi-person-fill-lock"></i></span>
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
        <b onClick={() => dispatch({type: "navigate", payload: "cart"})}>До Кошику</b>
    </div>;
}


const domRoot = document.getElementById("app-container");
const cp = domRoot.getAttribute("data-context-path");
const hp = domRoot.getAttribute("data-home-path");
// console.log(cp);
ReactDOM
    .createRoot(domRoot)
    .render(<App contextPath={cp} homePath={hp}/>);
