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
function App() {
    const [state, dispatch] = React.useReducer( reducer, initialState );
    React.useEffect( () => {
        let hash = window.location.hash;
        if( hash.length > 1 ) {
            dispatch( { type: "navigate", payload: hash.substring(1) } );
        }
    }, [] );
    return <AppContext.Provider value={ {state, dispatch} } >
        <h1>Крамниця</h1>
        <b onClick={() => dispatch({type: "navigate", payload: "home"})}>Домашня</b>
        <b onClick={() => dispatch({type: "navigate", payload: "cart"})}>Кошик</b>
        {state.page === 'home' && <Home />}
        {state.page === 'cart' && <Cart />}
    </AppContext.Provider>;
}

function Cart() {
    const {state, dispatch} = React.useContext( AppContext );
    return <div>
        <h2>Кошик</h2>
        <b onClick={() => dispatch({type: "navigate", payload: "home"})}>На Домашню</b>
    </div>;
}

function Home() {
    const {state, dispatch} = React.useContext( AppContext );
    return <div>
        <h2>Домашня</h2>
        <b onClick={() => dispatch({type: "navigate", payload: "cart"})}>До Кошику</b>
    </div>;
}

ReactDOM
    .createRoot(document.getElementById("app-container"))
    .render( <App /> );
