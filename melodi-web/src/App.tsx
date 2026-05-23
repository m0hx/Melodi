import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout.tsx'
import { HomePage } from './pages/HomePage.tsx'
import { SignInPage } from './pages/SignInPage.tsx'
import { SignUpPage } from './pages/SignUpPage.tsx'
import { InstrumentsPage } from './pages/InstrumentsPage.tsx'
import { InstrumentDetailPage } from './pages/InstrumentDetailPage.tsx'
import { PlaceholderPage } from './pages/PlaceholderPage.tsx'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/signin" element={<SignInPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/instruments" element={<InstrumentsPage />} />
        <Route path="/instruments/:instrumentId" element={<InstrumentDetailPage />} />
        <Route path="/cart" element={<PlaceholderPage title="Cart" />} />
        <Route path="/orders" element={<PlaceholderPage title="Orders" />} />
        <Route path="/rentals" element={<PlaceholderPage title="Rentals" />} />
        <Route path="/wishlist" element={<PlaceholderPage title="Wishlist" />} />
        <Route path="/profile" element={<PlaceholderPage title="Profile" />} />
        <Route path="/admin" element={<PlaceholderPage title="Admin" />} />
      </Route>
    </Routes>
  )
}
