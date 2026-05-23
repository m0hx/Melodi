import { type ReactNode } from 'react'
import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout.tsx'
import { RequireAuth } from './components/RequireAuth.tsx'
import { HomePage } from './pages/HomePage.tsx'
import { SignInPage } from './pages/SignInPage.tsx'
import { SignUpPage } from './pages/SignUpPage.tsx'
import { InstrumentsPage } from './pages/InstrumentsPage.tsx'
import { InstrumentDetailPage } from './pages/InstrumentDetailPage.tsx'
import { CartPage } from './pages/CartPage.tsx'
import { CheckoutPage } from './pages/CheckoutPage.tsx'
import { OrdersPage } from './pages/OrdersPage.tsx'
import { OrderDetailPage } from './pages/OrderDetailPage.tsx'
import { ProfilePage } from './pages/ProfilePage.tsx'
import { WishlistPage } from './pages/WishlistPage.tsx'
import { RentalsPage } from './pages/RentalsPage.tsx'

function Auth({ children }: { children: ReactNode }) {
  return <RequireAuth>{children}</RequireAuth>
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/signin" element={<SignInPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/instruments" element={<InstrumentsPage />} />
        <Route path="/instruments/:instrumentId" element={<InstrumentDetailPage />} />
        <Route
          path="/cart"
          element={
            <Auth>
              <CartPage />
            </Auth>
          }
        />
        <Route
          path="/checkout"
          element={
            <Auth>
              <CheckoutPage />
            </Auth>
          }
        />
        <Route
          path="/orders"
          element={
            <Auth>
              <OrdersPage />
            </Auth>
          }
        />
        <Route
          path="/orders/:orderId"
          element={
            <Auth>
              <OrderDetailPage />
            </Auth>
          }
        />
        <Route
          path="/rentals"
          element={
            <Auth>
              <RentalsPage />
            </Auth>
          }
        />
        <Route
          path="/wishlist"
          element={
            <Auth>
              <WishlistPage />
            </Auth>
          }
        />
        <Route
          path="/profile"
          element={
            <Auth>
              <ProfilePage />
            </Auth>
          }
        />
      </Route>
    </Routes>
  )
}
