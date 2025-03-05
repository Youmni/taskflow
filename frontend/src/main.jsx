import { StrictMode, Suspense  } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthProvider'
import { SnackbarProvider } from 'notistack'

import 'bootstrap/dist/css/bootstrap.min.css';
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <Suspense fallback={<div>Loading...</div>}>
      <BrowserRouter>
        <SnackbarProvider maxSnack={1} autoHideDuration={1500} anchorOrigin={{ vertical: "top", horizontal: "center" }}>
          <AuthProvider>
            <App />
          </AuthProvider>
        </SnackbarProvider>
      </BrowserRouter>
    </Suspense>
  </StrictMode>
);