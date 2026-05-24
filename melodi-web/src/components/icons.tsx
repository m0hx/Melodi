import type { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faArrowLeft,
  faBagShopping,
  faBars,
  faCalendarDays,
  faCartPlus,
  faCartShopping,
  faCheck,
  faCreditCard,
  faEye,
  faFloppyDisk,
  faGuitar,
  faHeart,
  faHouse,
  faImage,
  faKey,
  faPen,
  faPlus,
  faReceipt,
  faRightFromBracket,
  faRightToBracket,
  faRotateLeft,
  faShieldHalved,
  faStar,
  faStore,
  faTags,
  faTrash,
  faUser,
  faUserPlus,
  faUserSlash,
  faUsers,
  faXmark,
} from '@fortawesome/free-solid-svg-icons'
import type { ReactNode } from 'react'

const navIconMap: Record<string, IconDefinition> = {
  '/instruments': faGuitar,
  '/cart': faCartShopping,
  '/wishlist': faHeart,
  '/orders': faReceipt,
  '/rentals': faCalendarDays,
  '/profile': faUser,
  '/admin': faShieldHalved,
}

export function navIcon(href: string): IconDefinition | undefined {
  return navIconMap[href]
}

type FaProps = {
  icon: IconDefinition
  className?: string
}

export function Fa({ icon, className = 'size-3.5 shrink-0' }: FaProps) {
  return <FontAwesomeIcon icon={icon} className={className} aria-hidden />
}

type IconLabelProps = {
  icon: IconDefinition
  children: ReactNode
  className?: string
}

/** Icon + text for buttons and nav links (parent should use inline-flex items-center gap-2). */
export function IconLabel({ icon, children, className }: IconLabelProps) {
  return (
    <>
      <Fa icon={icon} className={className} />
      {children}
    </>
  )
}

export const icons = {
  arrowLeft: faArrowLeft,
  bag: faBagShopping,
  bars: faBars,
  calendar: faCalendarDays,
  cart: faCartShopping,
  cartPlus: faCartPlus,
  check: faCheck,
  creditCard: faCreditCard,
  eye: faEye,
  guitar: faGuitar,
  heart: faHeart,
  home: faHouse,
  image: faImage,
  key: faKey,
  pen: faPen,
  plus: faPlus,
  receipt: faReceipt,
  return: faRotateLeft,
  save: faFloppyDisk,
  shield: faShieldHalved,
  signIn: faRightToBracket,
  signOut: faRightFromBracket,
  signUp: faUserPlus,
  star: faStar,
  store: faStore,
  tags: faTags,
  trash: faTrash,
  user: faUser,
  userSlash: faUserSlash,
  users: faUsers,
  xmark: faXmark,
} as const

/** Use on Button/Link when showing an icon + label. */
export const withIcon = 'inline-flex items-center gap-2'
