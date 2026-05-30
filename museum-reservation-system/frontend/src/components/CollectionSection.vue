<template>
  <section class="collection-section py-5">
    <div class="container">
      <div class="text-center mb-4">
        <span class="status-pill mb-2">镇馆之宝</span>
        <h2 class="section-title">馆藏精粹</h2>
      </div>

      <div class="collection-carousel-wrapper">
        <button class="collection-nav collection-nav-prev" @click="prev" aria-label="上一件">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" viewBox="0 0 16 16">
            <path fill-rule="evenodd" d="M11 2.5a.5.5 0 0 1 .146.354l-5 5a.5.5 0 0 0 0 .708l5 5a.5.5 0 0 1-.708.708l-5-5a1.5 1.5 0 0 1 0-2.122l5-5a.5.5 0 0 1 .562-.054z"/>
          </svg>
        </button>

        <div class="collection-track-outer">
          <div
            class="collection-track"
            :style="{ transform: `translateX(-${currentIndex * slideWidth}px)` }"
          >
            <div
              v-for="(item, index) in artifacts"
              :key="index"
              class="collection-slide"
              :style="slideStyle"
            >
              <div class="artifact-card">
                <div class="artifact-img-wrapper">
                  <img :src="item.image" :alt="item.name" class="artifact-img" />
                  <div class="artifact-badge">{{ item.era }}</div>
                </div>
                <div class="artifact-body">
                  <h4 class="artifact-name">{{ item.name }}</h4>
                  <p class="artifact-desc">{{ item.description }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <button class="collection-nav collection-nav-next" @click="next" aria-label="下一件">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" viewBox="0 0 16 16">
            <path fill-rule="evenodd" d="M5 2.5a.5.5 0 0 0-.146.354l5 5a.5.5 0 0 1 0 .708l-5 5a.5.5 0 0 0 .708.708l5-5a1.5 1.5 0 0 0 0-2.122l-5-5a.5.5 0 0 0-.562-.054z"/>
          </svg>
        </button>
      </div>

      <div class="collection-dots mt-4">
        <button
          v-for="(_, index) in totalDots"
          :key="index"
          class="collection-dot"
          :class="{ active: index === currentDot }"
          @click="goToDot(index)"
          :aria-label="'第' + (index + 1) + '页'"
        ></button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import img1 from '../assets/images/青铜鼎.png'
import img2 from '../assets/images/生成文物与画作图片 (1).png'
import img3 from '../assets/images/生成文物与画作图片 (2).png'
import img4 from '../assets/images/生成文物与画作图片 (3).png'
import img5 from '../assets/images/生成文物与画作图片 (4).png'
import img6 from '../assets/images/生成文物与画作图片 (5).png'

const artifacts = [
  {
    image: img1,
    name: '青铜鼎',
    era: '商代',
    description: '商代青铜礼器之精品，纹饰精美，造型庄严，见证三千年礼乐文明。'
  },
  {
    image: img2,
    name: '青瓷莲花尊',
    era: '南朝',
    description: '南朝青瓷代表作，釉色温润如玉，莲花装饰精巧绝伦。'
  },
  {
    image: img3,
    name: '金缕玉衣',
    era: '汉代',
    description: '以金丝连缀玉片而成的汉代贵族殓服，工艺精湛，世所罕见。'
  },
  {
    image: img4,
    name: '唐三彩骆驼',
    era: '唐代',
    description: '盛唐时期彩釉陶器精品，色彩斑斓，生动展现丝路风情。'
  },
  {
    image: img5,
    name: '粉彩百花瓶',
    era: '清代',
    description: '清代官窑粉彩瓷器，百花竞放，色彩柔美，尽显皇家气象。'
  },
  {
    image: img6,
    name: '水墨山水图',
    era: '宋代',
    description: '宋代文人画经典之作，笔墨淋漓，意境深远，尽显东方美学。'
  }
]

const currentIndex = ref(0)
const slideWidth = ref(0)
const visibleSlides = ref(3)
let autoTimer = null

const totalDots = computed(() => {
  return Math.max(1, artifacts.length - visibleSlides.value + 1)
})

const currentDot = computed(() => currentIndex.value)

const slideStyle = computed(() => ({
  width: `${Math.max(slideWidth.value - 12, 180)}px`
}))

function measureSlide() {
  const outer = document.querySelector('.collection-track-outer')
  if (!outer) return
  const totalWidth = outer.offsetWidth
  const gap = 12
  visibleSlides.value = window.innerWidth < 576 ? 2 : window.innerWidth < 992 ? 3 : 4
  slideWidth.value = (totalWidth + gap) / visibleSlides.value
  const maxIndex = Math.max(0, artifacts.length - visibleSlides.value)
  if (currentIndex.value > maxIndex) {
    currentIndex.value = maxIndex
  }
}

function next() {
  const maxIndex = Math.max(0, artifacts.length - visibleSlides.value)
  currentIndex.value = currentIndex.value >= maxIndex ? 0 : currentIndex.value + 1
}

function prev() {
  const maxIndex = Math.max(0, artifacts.length - visibleSlides.value)
  currentIndex.value = currentIndex.value <= 0 ? maxIndex : currentIndex.value - 1
}

function goToDot(index) {
  currentIndex.value = index
}

function startAuto() {
  stopAuto()
  autoTimer = setInterval(next, 3500)
}

function stopAuto() {
  if (autoTimer) {
    clearInterval(autoTimer)
    autoTimer = null
  }
}

onMounted(() => {
  measureSlide()
  window.addEventListener('resize', measureSlide)
  startAuto()
})

onUnmounted(() => {
  window.removeEventListener('resize', measureSlide)
  stopAuto()
})
</script>
