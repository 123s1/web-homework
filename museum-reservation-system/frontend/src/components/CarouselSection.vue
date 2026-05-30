<template>
  <div id="heroCarousel" class="carousel slide hero-carousel" data-bs-ride="carousel">
    <div class="carousel-indicators">
      <button
        v-for="(_, index) in slides"
        :key="index"
        type="button"
        data-bs-target="#heroCarousel"
        :data-bs-slide-to="index"
        :class="{ active: index === 0 }"
        :aria-current="index === 0 ? 'true' : undefined"
        :aria-label="'幻灯片 ' + (index + 1)"
      ></button>
    </div>

    <div class="carousel-inner">
      <div
        v-for="(slide, index) in slides"
        :key="index"
        class="carousel-item"
        :class="{ active: index === 0 }"
      >
        <div class="carousel-bg" :style="{ backgroundImage: `url(${slide.image})` }">
          <div class="carousel-overlay"></div>
        </div>
        <div class="container position-relative carousel-caption-container">
          <div class="carousel-text">
            <span class="status-pill mb-3">{{ slide.tag }}</span>
            <h2 class="display-6 fw-bold mb-3">{{ slide.title }}</h2>
            <p class="lead mb-4">{{ slide.description }}</p>
            <RouterLink v-if="slide.link" :to="slide.link" class="btn btn-museum">
              {{ slide.btnText }}
            </RouterLink>
          </div>
        </div>
      </div>
    </div>

    <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
      <span class="carousel-control-prev-icon" aria-hidden="true"></span>
      <span class="visually-hidden">上一张</span>
    </button>
    <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
      <span class="carousel-control-next-icon" aria-hidden="true"></span>
      <span class="visually-hidden">下一张</span>
    </button>
  </div>
</template>

<script setup>
import img1 from '../assets/images/青铜鼎.png'
import img2 from '../assets/images/生成文物与画作图片 (1).png'
import img3 from '../assets/images/生成文物与画作图片 (2).png'

const slides = [
  {
    image: img1,
    tag: '分时预约 · 实名参观',
    title: '省博物馆参观预约系统',
    description: '线上实名预约、分时段入馆，提升参观体验并保障预约数据一致性。',
    link: '/slots',
    btnText: '立即预约'
  },
  {
    image: img2,
    tag: '精品展览 · 不容错过',
    title: '探索千年文明瑰宝',
    description: '漫步历史长河，近距离感受中华文明的璀璨与辉煌。',
    link: '/slots',
    btnText: '查看展览'
  },
  {
    image: img3,
    tag: '文化之旅 · 精彩纷呈',
    title: '开启您的文化之旅',
    description: '丰富多彩的文化活动与专题展览，等您来体验与发现。',
    link: '/slots',
    btnText: '了解详情'
  }
]
</script>
