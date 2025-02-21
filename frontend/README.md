# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

```
frontend
├─ .env
├─ .gitignore
├─ components.json
├─ Dockerfile
├─ eslint.config.js
├─ index.html
├─ jsconfig.json
├─ nginx.conf
├─ package-lock.json
├─ package.json
├─ postcss.config.js
├─ public
│  ├─ Advertisement
│  │  ├─ dummy01.jpg
│  │  ├─ dummy02.jpg
│  │  ├─ dummy03.jpg
│  │  ├─ dummy04.jpg
│  │  ├─ dummy05.jpg
│  │  ├─ dummy06.jpg
│  │  └─ heart.png
│  ├─ Pretendard-Regular.otf
│  └─ PretendardVariable.woff2
├─ README.md
├─ src
│  ├─ App.css
│  ├─ App.jsx
│  ├─ assets
│  │  ├─ BeforeOnBoarding
│  │  │  ├─ flower-bouquet.png
│  │  │  ├─ heartwithribbon.svg
│  │  │  └─ man.svg
│  │  ├─ file.svg
│  │  ├─ globe.svg
│  │  ├─ logo.png
│  │  ├─ next.svg
│  │  ├─ PretendardVariable.woff2
│  │  ├─ vercel.svg
│  │  ├─ VirginRoadCard
│  │  │  ├─ status1.svg
│  │  │  ├─ status2.svg
│  │  │  ├─ status3.svg
│  │  │  ├─ status4.svg
│  │  │  └─ tuxedo.png
│  │  └─ window.svg
│  ├─ components
│  │  ├─ appbar.jsx
│  │  ├─ common
│  │  │  ├─ Button.jsx
│  │  │  ├─ cards
│  │  │  │  ├─ AdvertisementCard.jsx
│  │  │  │  ├─ DDayCard.jsx
│  │  │  │  ├─ IconCard.jsx
│  │  │  │  └─ VirginRoadCard.jsx
│  │  │  ├─ Input.jsx
│  │  │  └─ NavBar.jsx
│  │  ├─ oauth-button.jsx
│  │  ├─ ui
│  │  │  ├─ avatar.jsx
│  │  │  ├─ button.jsx
│  │  │  ├─ dropdown-menu.jsx
│  │  │  ├─ input.jsx
│  │  │  ├─ slider.jsx
│  │  │  ├─ toast.jsx
│  │  │  └─ toaster.jsx
│  │  └─ VirginRoadList.jsx
│  ├─ constants
│  │  ├─ colors.jsx
│  │  ├─ fonts.jsx
│  │  └─ shadows.jsx
│  ├─ contexts
│  │  └─ theme.jsx
│  ├─ hooks
│  │  └─ use-toast.js
│  ├─ index.css
│  ├─ lib
│  │  ├─ axios.js
│  │  └─ utils.js
│  ├─ main.jsx
│  ├─ pages
│  │  ├─ AfterOnBoarding.jsx
│  │  ├─ auth
│  │  │  ├─ oauth
│  │  │  │  └─ callback.jsx
│  │  │  ├─ onboarding
│  │  │  │  ├─ level
│  │  │  │  │  ├─ age.jsx
│  │  │  │  │  ├─ gender.jsx
│  │  │  │  │  ├─ mbti.jsx
│  │  │  │  │  └─ phone.jsx
│  │  │  │  └─ OnBoarding.jsx
│  │  │  └─ register
│  │  │     ├─ level
│  │  │     │  ├─ id.jsx
│  │  │     │  ├─ name.jsx
│  │  │     │  ├─ password-check.jsx
│  │  │     │  └─ password.jsx
│  │  │     └─ Register.jsx
│  │  ├─ BeforeOnBoarding.jsx
│  │  ├─ Home.jsx
│  │  ├─ login
│  │  │  └─ login.jsx
│  │  ├─ NotFound.jsx
│  │  ├─ regist
│  │  │  └─ regist.jsx
│  │  └─ virginRoad
│  │     ├─ timecapsule
│  │     │  └─ TimeCapsule.jsx
│  │     └─ virginroad
│  │        └─ VirginRoad.jsx
│  ├─ store
│  │  ├─ auth.js
│  │  ├─ onboarding.js
│  │  └─ register.js
│  ├─ style
│  │  ├─ AfterOnBoarding.css
│  │  ├─ BeforeOnBoarding.css
│  │  ├─ common
│  │  │  ├─ Button.css
│  │  │  ├─ Input.css
│  │  │  └─ NavBar.css
│  │  ├─ component
│  │  │  ├─ AdvertisementCard.css
│  │  │  ├─ DDayCard.css
│  │  │  ├─ IconCard.css
│  │  │  └─ VirginRoadCard.css
│  │  ├─ Home.css
│  │  ├─ OnBoarding.css
│  │  ├─ pretendardvariable.css
│  │  ├─ Register.css
│  │  ├─ text.jsx
│  │  ├─ TimeCapsule.css
│  │  ├─ VirginRoad.css
│  │  └─ VirginRoadList.css
│  └─ utils
│     └─ axios.js
├─ tailwind.config.js
└─ vite.config.js

```