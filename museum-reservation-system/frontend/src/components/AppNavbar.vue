<template>
  <nav class="navbar navbar-expand-lg navbar-dark museum-navbar sticky-top">
    <div class="container">
      <RouterLink class="navbar-brand d-flex align-items-center gap-2 fw-bold" to="/">
        <span class="brand-mark">博</span>
        <span>省博物馆预约系统</span>
      </RouterLink>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#visitorNavbar">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div id="visitorNavbar" class="collapse navbar-collapse">
        <div class="navbar-nav ms-auto align-items-lg-center gap-lg-2">
          <RouterLink class="nav-link" to="/">首页</RouterLink>
          <RouterLink class="nav-link" to="/slots">参观预约</RouterLink>
          <RouterLink class="nav-link" to="/reservations/my">我的预约</RouterLink>
          <RouterLink class="btn btn-sm btn-light rounded-pill px-3 ms-lg-2" to="/visitor/login">游客登录</RouterLink>
          <RouterLink class="btn btn-sm btn-outline-light rounded-pill px-3" to="/admin/login">后台登录</RouterLink>
        </div>
      </div>
    </div>
  </nav>
</template>
